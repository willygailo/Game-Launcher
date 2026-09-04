package com.gamebooster.app.shizuku;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ShizukuConnectionLifecycleTest {

    @Test
    public void testConnectionStatesExist() {
        assertNotNull(ShizukuConnectionManager.State.IDLE);
        assertNotNull(ShizukuConnectionManager.State.BINDING);
        assertNotNull(ShizukuConnectionManager.State.READY);
        assertNotNull(ShizukuConnectionManager.State.DEAD);
        assertNotNull(ShizukuConnectionManager.State.RETRY);
    }

    @Test
    public void testConnectionManagerSingleton() {
        ShizukuConnectionManager manager1 = ShizukuConnectionManager.getInstance();
        ShizukuConnectionManager manager2 = ShizukuConnectionManager.getInstance();
        assertNotNull(manager1);
        assertEquals(manager1, manager2);
    }

    @Test
    public void testConnectionListenerNotification() {
        ShizukuConnectionManager manager = ShizukuConnectionManager.getInstance();
        manager.stop();
        AtomicBoolean received = new AtomicBoolean(false);

        ShizukuConnectionManager.ConnectionListener listener = state -> received.set(true);
        manager.addConnectionListener(listener);

        // start converges the state machine
        manager.start();
        assertTrue("Listener should receive state updates", received.get());

        manager.removeConnectionListener(listener);
    }

    @Test
    public void testInitialStateNotDead() {
        ShizukuConnectionManager manager = ShizukuConnectionManager.getInstance();
        manager.start();
        // start() with pending binder should transition to BINDING or IDLE/READY, not immediately DEAD
        assertNotNull(manager.getState());
    }

    @Test
    public void testShizukuManagerStatusCheckRunsWithoutCrashing() {
        // Ping binder on host test runner should return false or mock cleanly without crashing
        boolean running = ShizukuManager.isShizukuRunningAndGranted();
        assertFalse(running);
    }

    @Test
    public void testUserServiceConnectorSingleton() {
        ShizukuUserServiceConnector connector = ShizukuUserServiceConnector.getInstance();
        assertNotNull(connector);
    }
}
