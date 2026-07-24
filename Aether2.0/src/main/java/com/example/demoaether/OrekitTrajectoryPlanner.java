package com.example.demoaether;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.ode.events.Action;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.orekit.bodies.CelestialBody;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.ThirdBodyAttraction;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.forces.maneuvers.ImpulseManeuver;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.orbits.Orbit;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.AltitudeDetector;
import org.orekit.propagation.events.DateDetector;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.propagation.sampling.OrekitStepNormalizer;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.PVCoordinates;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Motor de precalculo orbital basado en Orekit.
 *
 * Quien lo llama:
 * - MissionSimulator llama precompute(config) antes de animar la UI.
 *
 * Que hace:
 * - Inicializa Orekit.
 * - Crea una orbita inicial en EME2000.
 * - Configura NumericalPropagator con DormandPrince853.
 * - Agrega gravedad terrestre 8x8, Luna, Sol y una maniobra TLI impulsiva.
 * - Muestra a la UI una lista de MissionState ya calculados; la UI no propaga Orekit en tiempo real.
 */
public class OrekitTrajectoryPlanner {

    private static final int MINIMUM_SAMPLES = 500;
    private static final double REENTRY_ALTITUDE_M = 120_000.0;

    private OrekitTrajectoryPlanner() {
    }

    /**
     * Precalcula la trayectoria completa con Orekit antes de animarla.
     *
     * @param config parametros orbitales y TLI definidos por el usuario
     * @return lista inmutable de MissionState y eventos detectados
     */
    public static MissionTrajectory precompute(MissionConfig config) {
        // Metodo principal llamado por MissionSimulator.
        // Devuelve MissionTrajectory con estados precalculados para que JavaFX solo anime resultados.
        OrekitInitializer.initialize();

        Orbit initialOrbit = OrbitFactory.createInitialOrbit(config);
        NumericalPropagator propagator = createPropagator(initialOrbit, config);
        AbsoluteDate start = initialOrbit.getDate();
        AbsoluteDate end = start.shiftedBy(config.getSimulationHours() * 3600.0);
        List<MissionState> states = new ArrayList<>();
        List<String> events = new ArrayList<>();

        double fixedStepSeconds = Math.max(1.0, end.durationFrom(start) / MINIMUM_SAMPLES);
        propagator.setStepHandler(new OrekitStepNormalizer(fixedStepSeconds, state -> {
            MissionState missionState = stateFromSpacecraftState(state, start);
            states.add(missionState);
        }));

        propagator.addEventDetector(createReentryDetector(events));
        propagator.propagate(start, end);
        addLunarPeriapsisEvent(states, events);

        return new MissionTrajectory(states, events, true);
    }

    private static NumericalPropagator createPropagator(Orbit initialOrbit, MissionConfig config) {
        // Configura el propagador fisico: integrador numerico, masa de nave, fuerzas y maniobra TLI.
        double[][] tolerances = NumericalPropagator.tolerances(10.0, initialOrbit, initialOrbit.getType());
        DormandPrince853Integrator integrator = new DormandPrince853Integrator(
                0.001,
                600.0,
                tolerances[0],
                tolerances[1]
        );
        integrator.setInitialStepSize(30.0);

        NumericalPropagator propagator = new NumericalPropagator(integrator);
        propagator.setInitialState(new SpacecraftState(initialOrbit, config.getSpacecraftMass()));

        // OAM-3: gravedad terrestre por armonicos esfericos 8x8.
        NormalizedSphericalHarmonicsProvider gravity = GravityFieldFactory.getNormalizedProvider(8, 8);
        propagator.addForceModel(new HolmesFeatherstoneAttractionModel(FramesFactory.getITRF(IERSConventions.IERS_2010, true), gravity));

        // OAM-3: atraccion de tercer cuerpo de Luna y Sol.
        propagator.addForceModel(new ThirdBodyAttraction(CelestialBodyFactory.getMoon()));
        propagator.addForceModel(new ThirdBodyAttraction(CelestialBodyFactory.getSun()));

        // OAM-4: maniobra TLI impulsiva. Orekit 12 nombra esta clase ImpulseManeuver.
        AbsoluteDate burnDate = initialOrbit.getDate().shiftedBy(config.getTliBurnOffsetHours() * 3600.0);
        DateDetector burnTrigger = new DateDetector(burnDate);
        Vector3D deltaV = new Vector3D(config.getTliDeltaVKms() * 1000.0, 0.0, 0.0);
        propagator.addEventDetector(new ImpulseManeuver(burnTrigger, deltaV, 320.0));

        return propagator;
    }

    private static AltitudeDetector createReentryDetector(List<String> events) {
        // Detector de reentrada: agrega un evento si la nave cruza 120 km descendiendo.
        OneAxisEllipsoid earth = new OneAxisEllipsoid(
                Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                Constants.WGS84_EARTH_FLATTENING,
                FramesFactory.getITRF(IERSConventions.IERS_2010, true)
        );
        return new AltitudeDetector(REENTRY_ALTITUDE_M, earth).withHandler((state, detector, increasing) -> {
            if (!increasing) {
                events.add("Reentrada detectada a 120 km: " + state.getDate());
            }
            return Action.CONTINUE;
        });
    }

    private static MissionState stateFromSpacecraftState(SpacecraftState state, AbsoluteDate start) {
        // Convierte SpacecraftState de Orekit a MissionState usado por la UI y los reportes.
        PVCoordinates pv = state.getPVCoordinates();
        Frame frame = state.getFrame();
        double elapsedSeconds = state.getDate().durationFrom(start);
        double distanceEarthKm = pv.getPosition().getNorm() / 1000.0;
        double altitudeKm = distanceEarthKm - Constants.WGS84_EARTH_EQUATORIAL_RADIUS / 1000.0;
        double distanceMoonKm = distanceToMoonKm(pv, frame, state.getDate());
        return new MissionState(
                elapsedSeconds,
                pv.getPosition().getX() / 1000.0,
                pv.getPosition().getY() / 1000.0,
                pv.getPosition().getZ() / 1000.0,
                pv.getVelocity().getNorm() / 1000.0,
                distanceEarthKm,
                distanceMoonKm,
                altitudeKm
        );
    }

    private static double distanceToMoonKm(PVCoordinates spacecraftPv, Frame frame, AbsoluteDate date) {
        // Calcula distancia nave-Luna consultando la posicion lunar en el mismo frame.
        CelestialBody moon = CelestialBodyFactory.getMoon();
        Vector3D moonPosition = moon.getPVCoordinates(date, frame).getPosition();
        return Vector3D.distance(spacecraftPv.getPosition(), moonPosition) / 1000.0;
    }

    private static void addLunarPeriapsisEvent(List<MissionState> states, List<String> events) {
        // Busca el punto de mayor acercamiento a la Luna entre los estados ya muestreados.
        MissionState closest = null;
        for (MissionState state : states) {
            if (closest == null || state.getDistanceMoon() < closest.getDistanceMoon()) {
                closest = state;
            }
        }
        if (closest != null) {
            events.add(String.format(Locale.US,
                    "Periapsis lunar aproximado: %.0f km a T+%.0f s",
                    closest.getDistanceMoon(),
                    closest.getElapsedTime()));
        }
    }
}
