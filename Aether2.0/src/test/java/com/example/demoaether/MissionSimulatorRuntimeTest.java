package com.example.demoaether;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MissionSimulatorRuntimeTest {

    @Test
    void simulationEmitsMultipleStatesQuickly() throws Exception {
        MissionConfig config = new MissionConfig();
        config.setSimulationHours(1);
        config.setSimulationStepSeconds(60);

        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger errors = new AtomicInteger();
        MissionSimulator simulator = new MissionSimulator(config);
        simulator.setSimulationListener(new SimulationListener() {
            @Override
            public void onSimulationStarted() {
            }

            @Override
            public void onStateUpdated(MissionState state) {
                latch.countDown();
                if (latch.getCount() == 0) {
                    simulator.stopSimulation();
                }
            }

            @Override
            public void onSimulationFinished() {
            }

            @Override
            public void onSimulationError(Exception exception) {
                errors.incrementAndGet();
            }
        });

        Thread thread = new Thread(simulator::startSimulation);
        thread.setDaemon(true);
        thread.start();

        assertTrue(latch.await(8, TimeUnit.SECONDS), "La simulacion no emitio estados a tiempo.");
        assertTrue(errors.get() == 0, "La simulacion reporto errores.");
    }
}
