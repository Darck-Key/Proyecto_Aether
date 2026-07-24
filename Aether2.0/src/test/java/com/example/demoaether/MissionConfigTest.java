package com.example.demoaether;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MissionConfigTest {

    @Test
    void defaultConfigIsValid() {
        MissionConfig config = new MissionConfig();

        assertDoesNotThrow(config::validate);
    }

    @Test
    void defaultParkingOrbitMatchesE4Requirement() {
        MissionConfig config = new MissionConfig();

        assertEquals(185.0, config.getInitialAltitude(), 0.001);
        assertEquals(0.0, config.getEccentricity(), 0.001);
    }

    @Test
    void invalidAltitudeIsRejected() {
        MissionConfig config = new MissionConfig();
        config.setInitialAltitude(50);

        assertThrows(IllegalArgumentException.class, config::validate);
    }
}
