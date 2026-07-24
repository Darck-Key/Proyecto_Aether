package com.example.demoaether;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.orekit.bodies.CelestialBody;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.forces.gravity.NewtonianAttraction;
import org.orekit.forces.gravity.ThirdBodyAttraction;
import org.orekit.frames.Frame;
import org.orekit.orbits.Orbit;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.Constants;
import org.orekit.utils.PVCoordinates;

/**
 * Motor de simulacion de la mision.
 *
 * Quien llama:
 * - HelloController.startSimulation() crea esta clase y la ejecuta en un hilo aparte.
 * - HelloController.calculateOrbit() usa calculateInitialState() para un calculo rapido.
 *
 * A quien llama:
 * - OrekitInitializer prepara datos de Orekit.
 * - OrekitTrajectoryPlanner intenta crear la trayectoria fisica.
 * - MissionLogger escribe CSV.
 * - SimulationListener notifica a la interfaz cada cambio de estado.
 */
public class MissionSimulator {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double MOON_DISTANCE_KM = 384400.0;

    private final MissionConfig config;
    private SimulationListener listener;
    private MissionState currentState;
    private MissionLogger logger;
    private volatile boolean running;
    private volatile boolean paused;

    /**
     * Crea un simulador para una configuracion orbital.
     *
     * @param config parametros de mision que se pasaran a OrekitTrajectoryPlanner
     */
    public MissionSimulator(MissionConfig config) {
        this.config = config;
    }

    /**
     * Registra el listener que recibira estados y eventos de simulacion.
     *
     * @param listener normalmente HelloController
     */
    public void setSimulationListener(SimulationListener listener) {
        // Registra el receptor de eventos; normalmente es HelloController.
        this.listener = listener;
    }

    /**
     * Devuelve el ultimo estado emitido por la simulacion.
     *
     * @return estado actual o null si aun no se emitio ninguno
     */
    public MissionState getCurrentState() {
        return currentState;
    }

    /**
     * Indica si el ciclo de simulacion sigue activo.
     *
     * @return true si el motor esta recorriendo estados
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Indica si la simulacion esta pausada.
     *
     * @return true si el bucle esta esperando reanudacion
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Alterna entre pausa y reanudacion.
     */
    public void togglePaused() {
        paused = !paused;
    }

    /**
     * Calcula un estado inicial usando Orekit con fallback visual si algo falla.
     *
     * @param config parametros de mision
     * @return primer estado orbital disponible
     */
    public static MissionState calculateInitialState(MissionConfig config) {
        // Calcula el primer estado con Orekit; si falla, usa un estado inicial simple para no romper la UI.
        try {
            OrekitInitializer.initialize();
            MissionTrajectory trajectory = OrekitTrajectoryPlanner.precompute(config);
            return trajectory.getStates().isEmpty() ? fallbackInitialState(config) : trajectory.getStates().get(0);
        } catch (RuntimeException exception) {
            return fallbackInitialState(config);
        }
    }

    /**
     * Ejecuta la simulacion completa.
     *
     * <p>Este metodo debe correr fuera del hilo JavaFX. Precalcula trayectoria,
     * emite MissionState al listener y registra CSV con MissionLogger.</p>
     */
    public void startSimulation() {
        // Bucle principal de animacion: recorre MissionTrajectory, escribe CSV y notifica al listener.
        running = true;
        paused = false;

        try {
            logger = new MissionLogger("mission-data.csv");
            if (listener != null) {
                listener.onSimulationStarted();
            }

            MissionTrajectory trajectory = buildTrajectorySafely();
            int step = config.getSimulationStepSeconds();
            long delayMillis = Math.max(80L, Math.round((step * 1000.0) / Math.max(1, config.getSimulationSpeed())));

            for (MissionState state : trajectory.getStates()) {
                while (paused && running) {
                    Thread.sleep(200);
                }
                if (!running) {
                    break;
                }

                currentState = state;

                if (logger != null) {
                    logger.logState(currentState);
                }
                if (listener != null) {
                    listener.onStateUpdated(currentState);
                }

                Thread.sleep(delayMillis);
            }

            if (logger != null) {
                logger.close();
            }
            running = false;
            if (listener != null) {
                listener.onSimulationFinished();
            }
        } catch (Exception e) {
            running = false;
            if (logger != null) {
                logger.close();
            }
            if (listener != null) {
                listener.onSimulationError(e);
            }
        }
    }

    /**
     * Solicita detener el bucle de simulacion.
     */
    public void stopSimulation() {
        // Bandera usada por el bucle principal para terminar sin forzar el hilo.
        running = false;
        paused = false;
    }

    private static MissionState fallbackInitialState(MissionConfig config) {
        // Estado minimo si Orekit no puede iniciar; mantiene telemetria y reportes funcionales.
        double distanceEarth = EARTH_RADIUS_KM + config.getInitialAltitude();
        double distanceMoon = Math.abs(MOON_DISTANCE_KM - distanceEarth);
        return new MissionState(
                0,
                distanceEarth,
                0,
                0,
                config.getInitialVelocity(),
                distanceEarth,
                distanceMoon,
                config.getInitialAltitude()
        );
    }

    private MissionTrajectory buildTrajectorySafely() {
        // Ruta preferida: OrekitTrajectoryPlanner. Ruta alternativa: trayectoria visual local.
        try {
            return OrekitTrajectoryPlanner.precompute(config);
        } catch (RuntimeException exception) {
            return createFallbackTrajectory(exception);
        }
    }

    private static MissionState stateFromSpacecraftState(SpacecraftState state, int elapsedSeconds) {
        // Convierte unidades de Orekit de metros a kilometros y las adapta al modelo MissionState.
        Orbit orbit = state.getOrbit();
        PVCoordinates pv = state.getPVCoordinates();
        double x = pv.getPosition().getX() / 1000;
        double y = pv.getPosition().getY() / 1000;
        double z = pv.getPosition().getZ() / 1000;
        double velocity = pv.getVelocity().getNorm() / 1000;
        double distanceEarth = pv.getPosition().getNorm() / 1000;
        double distanceMoon = distanceToMoonKm(pv, orbit.getFrame(), state.getDate());
        double altitude = distanceEarth - EARTH_RADIUS_KM;

        return new MissionState(
                elapsedSeconds,
                x,
                y,
                z,
                velocity,
                distanceEarth,
                distanceMoon,
                altitude
        );
    }

    private static MissionState stateFromOrbit(Orbit orbit, int elapsedSeconds) {
        PVCoordinates pv = orbit.getPVCoordinates();
        double x = pv.getPosition().getX() / 1000;
        double y = pv.getPosition().getY() / 1000;
        double z = pv.getPosition().getZ() / 1000;
        double velocity = pv.getVelocity().getNorm() / 1000;
        double distanceEarth = pv.getPosition().getNorm() / 1000;
        double distanceMoon = Math.abs(MOON_DISTANCE_KM - distanceEarth);
        double altitude = distanceEarth - EARTH_RADIUS_KM;

        return new MissionState(
                elapsedSeconds,
                x,
                y,
                z,
                velocity,
                distanceEarth,
                distanceMoon,
                altitude
        );
    }

    private MissionState stateFromConfig(int elapsedSeconds) {
        double distanceEarth = EARTH_RADIUS_KM + config.getInitialAltitude();
        double distanceMoon = Math.abs(MOON_DISTANCE_KM - distanceEarth);
        return new MissionState(
                elapsedSeconds,
                distanceEarth,
                0,
                0,
                config.getInitialVelocity(),
                distanceEarth,
                distanceMoon,
                config.getInitialAltitude()
        );
    }

    private MissionTrajectory createFallbackTrajectory(RuntimeException exception) {
        // Trayectoria de respaldo cuando la propagacion real falla; conserva avance, fases y reporte.
        int endSecond = config.getSimulationHours() * 3600;
        int step = Math.max(1, config.getSimulationStepSeconds());
        java.util.List<MissionState> states = new java.util.ArrayList<>();
        java.util.List<String> events = new java.util.ArrayList<>();
        events.add("Fallback visual activado: " + exception.getMessage());
        states.add(stateFromConfig(0));
        for (int second = step; second <= endSecond; second += step) {
            states.add(stateFromMissionProgress(second, endSecond));
        }
        return new MissionTrajectory(states, events, false);
    }

    private MissionState stateFromMissionProgress(int elapsedSeconds, int endSecond) {
        // Modelo visual simple de ida a la Luna y regreso para que el mapa siga animandose.
        double progress = Math.min(1.0, Math.max(0.0, elapsedSeconds / (double) Math.max(1, endSecond)));
        double outbound = smoothStep(Math.min(1.0, progress / 0.58));
        double returnLeg = progress <= 0.58 ? 0.0 : smoothStep((progress - 0.58) / 0.42);
        double lunarApproach = progress <= 0.58 ? outbound : 1.0 - returnLeg;

        double distanceEarth = EARTH_RADIUS_KM + config.getInitialAltitude()
                + lunarApproach * (MOON_DISTANCE_KM - EARTH_RADIUS_KM - config.getInitialAltitude() - 6500);
        double distanceMoon = Math.max(1800, Math.abs(MOON_DISTANCE_KM - distanceEarth));
        double altitude = Math.max(config.getInitialAltitude(), distanceEarth - EARTH_RADIUS_KM);
        double velocity = config.getInitialVelocity()
                + Math.sin(progress * Math.PI * 2.0) * 0.55
                - Math.max(0, progress - 0.7) * 1.2;
        double angle = progress * Math.PI * 1.7;

        return new MissionState(
                elapsedSeconds,
                Math.cos(angle) * distanceEarth,
                Math.sin(angle) * distanceEarth,
                Math.sin(progress * Math.PI) * 35000,
                Math.max(0.8, velocity),
                distanceEarth,
                distanceMoon,
                altitude
        );
    }

    private static double smoothStep(double value) {
        // Suaviza transiciones visuales de la trayectoria de respaldo.
        double t = Math.min(1.0, Math.max(0.0, value));
        return t * t * (3.0 - 2.0 * t);
    }

    private static NumericalPropagator createNumericalPropagator(Orbit initialOrbit, MissionConfig config) {
        // Propagador numerico base con gravedad terrestre, lunar y solar.
        double[][] tolerances = NumericalPropagator.tolerances(10.0, initialOrbit, initialOrbit.getType());
        DormandPrince853Integrator integrator = new DormandPrince853Integrator(
                0.001,
                Math.max(60.0, config.getSimulationStepSeconds()),
                tolerances[0],
                tolerances[1]
        );
        integrator.setInitialStepSize(Math.min(30.0, Math.max(1.0, config.getSimulationStepSeconds())));

        NumericalPropagator propagator = new NumericalPropagator(integrator);
        propagator.setInitialState(new SpacecraftState(initialOrbit, config.getSpacecraftMass()));
        propagator.addForceModel(new NewtonianAttraction(Constants.WGS84_EARTH_MU));
        propagator.addForceModel(new ThirdBodyAttraction(CelestialBodyFactory.getMoon()));
        propagator.addForceModel(new ThirdBodyAttraction(CelestialBodyFactory.getSun()));
        return propagator;
    }

    private static double distanceToMoonKm(PVCoordinates spacecraftPv, Frame frame, AbsoluteDate date) {
        // Consulta la posicion lunar en Orekit para calcular distancia nave-Luna.
        CelestialBody moon = CelestialBodyFactory.getMoon();
        Vector3D moonPosition = moon.getPVCoordinates(date, frame).getPosition();
        return Vector3D.distance(spacecraftPv.getPosition(), moonPosition) / 1000.0;
    }
}
