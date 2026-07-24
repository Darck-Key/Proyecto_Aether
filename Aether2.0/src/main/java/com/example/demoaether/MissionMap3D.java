package com.example.demoaether;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

/**
 * Vista 3D del mapa de mision.
 *
 * Quien llama:
 * - HelloController.initializeMissionMap() muestra el placeholder.
 * - HelloController.applyMissionState() llama createScene(state) para refrescar la trayectoria.
 * - Los botones de zoom llaman zoomIn(), zoomOut() y resetCamera().
 *
 * Que hace:
 * - Dibuja Tierra, Luna, estrellas, trayectoria discontinua y nave triangular.
 * - No calcula fisica orbital; solo representa visualmente el MissionState recibido.
 */
public class MissionMap3D {

    private static final double WIDTH = 900;
    private static final double HEIGHT = 370;
    private static final int ROUTE_POINTS = 120;
    private static double cameraDistance = -700;

    private MissionMap3D() {
    }

    /**
     * Crea una escena inicial antes de que exista telemetria real.
     *
     * @return nodo JavaFX listo para insertarse en missionMapContainer
     */
    public static Node createPlaceholder() {
        // Estado inicial del mapa antes de tener telemetria calculada.
        MissionState initial = new MissionState(0, 6671, 0, 0, 7.8, 6671, 377729, 300);
        return createScene(initial);
    }

    /**
     * Crea la escena 3D a partir del estado actual de la mision.
     *
     * @param state telemetria actual usada para inferir progreso visual
     * @return subescena JavaFX con Tierra, Luna, trayectoria, nave y estrellas
     */
    public static Node createScene(MissionState state) {
        // Ensambla toda la escena 3D a partir del progreso de MissionState.
        double progress = Math.min(1.0, Math.max(0.0, state.getElapsedTime() / (10.0 * 3600.0)));
        double[] craft = routePoint(progress);

        Group root = new Group();
        root.getChildren().addAll(
                createStars(),
                createEarthOrbit(),
                createRoute(progress),
                createEarth(),
                createMoon(),
                createSpacecraft(craft)
        );

        PointLight light = new PointLight(Color.web("#E7D8F0"));
        light.setTranslateX(-120);
        light.setTranslateY(-220);
        light.setTranslateZ(-320);
        root.getChildren().add(light);

        root.getTransforms().add(new Rotate(-8, Rotate.X_AXIS));
        root.getTransforms().add(new Rotate(-18, Rotate.Y_AXIS));

        SubScene scene = new SubScene(root, WIDTH, HEIGHT, true, null);
        scene.setFill(Color.web("#05030D"));

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(cameraDistance);
        camera.setNearClip(0.1);
        camera.setFarClip(2000);
        scene.setCamera(camera);

        scene.setOnScroll(event -> {
            double next = camera.getTranslateZ() + event.getDeltaY() * 0.5;
            cameraDistance = Math.max(-1000, Math.min(-420, next));
            camera.setTranslateZ(cameraDistance);
        });

        return scene;
    }

    /**
     * Acerca la camara del mapa.
     */
    public static void zoomIn() {
        // Llamado por el boton Zoom + del FXML.
        cameraDistance = Math.min(-420, cameraDistance + 80);
    }

    /**
     * Aleja la camara del mapa.
     */
    public static void zoomOut() {
        // Llamado por el boton Zoom - del FXML.
        cameraDistance = Math.max(-1000, cameraDistance - 80);
    }

    /**
     * Devuelve la camara a la distancia por defecto.
     */
    public static void resetCamera() {
        // Restaura la camara a la distancia inicial del mapa.
        cameraDistance = -700;
    }

    private static Group createEarth() {
        // Crea la Tierra estilizada con tono azul electrico solicitado.
        Group group = new Group();

        Sphere glow = sphere(48, "#0D3E88");
        glow.setTranslateX(-255);
        glow.setOpacity(0.38);

        Sphere earth = sphere(42, "#6DA8FF");
        earth.setTranslateX(-255);

        Sphere atmosphere = sphere(43, "#E7F4FF");
        atmosphere.setTranslateX(-255);
        atmosphere.setOpacity(0.08);

        group.getChildren().addAll(glow, earth, atmosphere);
        return group;
    }

    private static Group createMoon() {
        // Crea la Luna estilizada con tono lavanda claro solicitado.
        Group group = new Group();
        Sphere moon = sphere(24, "#D8CAD5");
        moon.setTranslateX(275);
        moon.setTranslateY(-18);
        group.getChildren().add(moon);
        return group;
    }

    private static Group createRoute(double progress) {
        // Dibuja la trayectoria completa y resalta la parte recorrida segun progress.
        Group group = new Group();
        double[] previous = null;
        for (int i = 0; i <= ROUTE_POINTS; i++) {
            double t = i / (double) ROUTE_POINTS;
            double[] current = routePoint(t);
            if (previous != null && i % 8 < 5) {
                String base = t < 0.5 ? "#A85CFF" : "#2F9CFF";
                String color = t <= progress ? "#6FD6FF" : base;
                group.getChildren().add(segment(previous, current, color, t <= progress ? 1.2 : 0.9));
            }
            previous = current;
        }
        return group;
    }

    private static Group createEarthOrbit() {
        // Dibuja una orbita terrestre discontinua como referencia visual.
        Group group = new Group();
        double[] previous = null;
        for (int i = 0; i <= 96; i++) {
            double a = Math.PI * 2 * i / 96.0;
            double[] current = {
                    -255 + Math.cos(a) * 82,
                    -2 + Math.sin(a) * 54,
                    20 + Math.sin(a) * 10
            };
            if (previous != null && i % 7 < 4) {
                group.getChildren().add(segment(previous, current, "#2F9CFF", 0.75));
            }
            previous = current;
        }
        return group;
    }

    private static Group createSpacecraft(double[] position) {
        // Coloca la nave como triangulo malva en la posicion actual de la ruta.
        Group group = new Group();

        Sphere halo = sphere(10, "#3E1B78");
        halo.setTranslateX(position[0]);
        halo.setTranslateY(position[1]);
        halo.setTranslateZ(position[2] + 4);
        halo.setOpacity(0.38);

        MeshView ship = spacecraftTriangle("#A46C93");
        ship.setTranslateX(position[0]);
        ship.setTranslateY(position[1]);
        ship.setTranslateZ(position[2] - 7);
        ship.getTransforms().add(new Rotate(-18, Rotate.Z_AXIS));
        ship.getTransforms().add(new Rotate(20, Rotate.Y_AXIS));

        group.getChildren().addAll(halo, ship);
        return group;
    }

    private static Group createStars() {
        // Fondo estelar con variaciones de #c79cff, #a46cff y #6da8ff.
        Group group = new Group();
        String[] starColors = {"#C79CFF", "#A46CFF", "#6DA8FF"};
        for (int i = 0; i < 150; i++) {
            Sphere star = sphere(i % 13 == 0 ? 1.15 : i % 5 == 0 ? 0.75 : 0.45, starColors[i % starColors.length]);
            star.setTranslateX(-420 + ((i * 113) % 840));
            star.setTranslateY(-158 + ((i * 67) % 300));
            star.setTranslateZ(-230 - ((i * 31) % 220));
            star.setOpacity(i % 4 == 0 ? 0.82 : i % 3 == 0 ? 0.58 : 0.38);
            group.getChildren().add(star);
        }
        return group;
    }

    private static double[] routePoint(double t) {
        // Curva por tramos: salida de Tierra, sobrevuelo lunar y retorno.
        if (t <= 0.44) {
            double u = t / 0.44;
            return cubic(
                    new double[]{-214, 10, 44},
                    new double[]{-110, -138, 32},
                    new double[]{145, -132, 12},
                    new double[]{250, -48, -18},
                    u
            );
        }

        if (t <= 0.60) {
            double u = (t - 0.44) / 0.16;
            double angle = Math.toRadians(-140 + 275 * u);
            return new double[]{
                    275 + Math.cos(angle) * 43,
                    -18 + Math.sin(angle) * 42,
                    -24 + Math.sin(angle) * 8
            };
        }

        double u = (t - 0.60) / 0.40;
        return cubic(
                new double[]{244, 14, -18},
                new double[]{118, 122, 4},
                new double[]{-96, 116, 24},
                new double[]{-214, 38, 42},
                u
        );
    }

    private static double[] cubic(double[] p0, double[] p1, double[] p2, double[] p3, double t) {
        // Bezier cubica usada para suavizar los tramos largos de la trayectoria.
        double u = 1.0 - t;
        double a = u * u * u;
        double b = 3 * u * u * t;
        double c = 3 * u * t * t;
        double d = t * t * t;
        return new double[]{
                a * p0[0] + b * p1[0] + c * p2[0] + d * p3[0],
                a * p0[1] + b * p1[1] + c * p2[1] + d * p3[1],
                a * p0[2] + b * p1[2] + c * p2[2] + d * p3[2]
        };
    }

    private static Cylinder segment(double[] from, double[] to, String color, double radius) {
        // Convierte dos puntos 3D en un cilindro delgado que actua como linea.
        Point3D start = new Point3D(from[0], from[1], from[2]);
        Point3D end = new Point3D(to[0], to[1], to[2]);
        Point3D diff = end.subtract(start);
        double height = diff.magnitude();

        Cylinder cylinder = new Cylinder(radius, height);
        cylinder.setMaterial(material(color));
        cylinder.setTranslateX((from[0] + to[0]) / 2.0);
        cylinder.setTranslateY((from[1] + to[1]) / 2.0);
        cylinder.setTranslateZ((from[2] + to[2]) / 2.0);

        Point3D yAxis = new Point3D(0, 1, 0);
        Point3D axis = yAxis.crossProduct(diff);
        double angle = Math.toDegrees(Math.acos(yAxis.normalize().dotProduct(diff.normalize())));
        if (axis.magnitude() > 0.0001) {
            cylinder.getTransforms().add(new Rotate(angle, axis));
        }
        return cylinder;
    }

    private static MeshView spacecraftTriangle(String color) {
        // Malla triangular simple para representar la nave sin usar una esfera.
        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().addAll(0, -13, 0, -11, 10, 0, 11, 10, 0);
        mesh.getTexCoords().addAll(0, 0);
        mesh.getFaces().addAll(0, 0, 1, 0, 2, 0);

        MeshView view = new MeshView(mesh);
        view.setCullFace(CullFace.NONE);
        view.setDrawMode(DrawMode.FILL);
        view.setMaterial(material(color));
        return view;
    }

    private static Text createLabel(String text, double x, double y, double z, String color, int size) {
        Text label = new Text(text);
        label.setFill(Color.web(color));
        label.setFont(Font.font("Consolas", size));
        label.setTranslateX(x);
        label.setTranslateY(y);
        label.setTranslateZ(z);
        return label;
    }

    private static Sphere sphere(double radius, String color) {
        // Helper visual para crear esferas con material uniforme.
        Sphere sphere = new Sphere(radius);
        sphere.setMaterial(material(color));
        return sphere;
    }

    private static PhongMaterial material(String color) {
        // Material comun para mantener brillo y color consistentes en el mapa.
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.web(color));
        material.setSpecularColor(Color.web("#ffffff"));
        return material;
    }
}
