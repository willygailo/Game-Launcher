package com.gamebooster.app.shizuku;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class OsVersionGuardTest {

    @Test
    public void testReliableSdkIntReturnsPositive() {
        int sdk = OsVersionGuard.getReliableSdkInt();
        assertTrue("SDK level should be >= 1, but was: " + sdk, sdk >= 1);
    }

    @Test
    public void testReliableReleaseNotNull() {
        String release = OsVersionGuard.getReliableRelease();
        assertNotNull("Release version should not be null", release);
        assertTrue("Release string should not be empty", !release.trim().isEmpty());
    }

    @Test
    public void testOsVersionName() {
        String name = OsVersionGuard.getOsVersionName();
        assertNotNull("OS Version name should not be null", name);
        assertTrue("OS Version name should start with 'Android'", name.startsWith("Android"));
    }

    @Test
    public void testVersionChecksConsistency() {
        int sdk = OsVersionGuard.getReliableSdkInt();
        if (sdk >= 36) {
            assertTrue(OsVersionGuard.isAndroid16OrAbove());
            assertTrue(OsVersionGuard.isAndroid15OrAbove());
            assertTrue(OsVersionGuard.isAndroid14OrAbove());
            assertTrue(OsVersionGuard.isAndroid13OrAbove());
        } else if (sdk >= 35) {
            assertTrue(OsVersionGuard.isAndroid15OrAbove());
            assertTrue(OsVersionGuard.isAndroid14OrAbove());
            assertTrue(OsVersionGuard.isAndroid13OrAbove());
        } else if (sdk >= 34) {
            assertTrue(OsVersionGuard.isAndroid14OrAbove());
            assertTrue(OsVersionGuard.isAndroid13OrAbove());
        } else if (sdk >= 33) {
            assertTrue(OsVersionGuard.isAndroid13OrAbove());
        }
    }
}
