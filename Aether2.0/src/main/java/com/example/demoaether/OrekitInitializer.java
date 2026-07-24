package com.example.demoaether;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;

/**
 * Inicializador de datos Orekit.
 *
 * Quien llama:
 * - HelloController.calculateOrbit().
 * - MissionSimulator.calculateInitialState().
 * - OrekitTrajectoryPlanner.precompute().
 *
 * Que hace:
 * - Localiza src/main/resources/orekit-data.
 * - Registra esa carpeta en DataProvidersManager para que Orekit pueda cargar efemerides y modelos.
 */
public class OrekitInitializer {

    private static boolean initialized;

    /**
     * Inicializa el contexto de datos de Orekit una sola vez por sesion.
     */
    public static synchronized void initialize() {
        // Metodo sincronizado para cargar Orekit una sola vez durante toda la sesion.
        if (initialized) {
            return;
        }

        try {
            File orekitData = locateOrekitData();

            if (!orekitData.exists() || !orekitData.isDirectory()) {
                throw new IllegalStateException(
                        "No se encontró la carpeta orekit-data en: " + orekitData.getAbsolutePath()
                );
            }

            DataProvidersManager manager =
                    DataContext.getDefault()
                            .getDataProvidersManager();

            manager.addProvider(
                    new DirectoryCrawler(orekitData)
            );

            initialized = true;
            System.out.println("Orekit inicializado correctamente desde: " + orekitData.getAbsolutePath());

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error inicializando Orekit",
                    e
            );
        }
    }

    private static File locateOrekitData() throws URISyntaxException {
        // Primero busca en arbol de desarrollo; si corre empaquetado, busca como recurso del classpath.
        File sourceTreeData = new File("src/main/resources/orekit-data");
        if (sourceTreeData.exists()) {
            return sourceTreeData;
        }

        URL resource = OrekitInitializer.class.getResource("/orekit-data");
        if (resource != null && "file".equals(resource.getProtocol())) {
            return new File(resource.toURI());
        }

        return sourceTreeData;
    }
}
