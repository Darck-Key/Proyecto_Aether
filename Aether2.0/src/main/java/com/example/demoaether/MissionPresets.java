package com.example.demoaether;


/**
 * Misiones preconfiguradas.
 * Contiene valores reales o aproximados
 * para iniciar simulaciones.
 */
public class MissionPresets {


    public static MissionConfig createArtemisII() {


        MissionConfig config = new MissionConfig();



        // Datos de la misión

        config.setMissionName(
                "Artemis II"
        );


        config.setSpacecraftName(
                "Orion"
        );



        // Masa aproximada cápsula Orion

        config.setSpacecraftMass(
                26000
        );



        // Órbita inicial aproximada

        config.setInitialAltitude(
                300
        );



        config.setInitialVelocity(
                10.8
        );



        // Duración de simulación

        config.setSimulationHours(
                72
        );



        // Mostrar cada 3 horas

        config.setSimulationStepSeconds(
                10800
        );



        config.setSimulationSpeed(
                1
        );



        config.setSaveReports(
                true
        );



        return config;

    }


}