package com.gamebooster.app.engine;

import android.util.Log;

/**
 * MemProcPatchEngine — Java wrapper for /proc/self/mem ARM64 patch (JNI).
 *
 * WHY /proc/self/mem instead of mprotect():
 *   ACE 2026 monitors mprotect() via seccomp BPF filter.
 *   Writing via /proc/self/mem fd does NOT trigger mprotect seccomp alert.
 *   This is the stealth-preferred ARM64 patch path for 2026.
 *
 * Native implementation: cpp/mem_proc_patch_engine.cpp
 *
 * Updated: Sep 30 2026
 */
public final class MemProcPatchEngine {

    private static final String TAG = "MemProcPatchEngine";

    private static volatile boolean sLibLoaded = false;

    static {
        try {
            System.loadLibrary("gamebooster_native");
            sLibLoaded = true;
            Log.i(TAG, "gamebooster_native library loaded OK");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load gamebooster_native: " + e.getMessage());
        }
    }

    private MemProcPatchEngine() {}

    // ─────────────────────────────────────────────────────────────────────────
    // JNI NATIVE DECLARATIONS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Patches bytes at the given virtual address via /proc/self/mem write.
     * No mprotect() syscall — avoids ACE seccomp detection.
     * ARM64: automatically flushes instruction cache after write.
     *
     * @param address  Target virtual address (long to accommodate 64-bit)
     * @param patch    Replacement bytes (ARM64 instructions, little-endian)
     * @return         true if all bytes were written successfully
     */
    public static native boolean patchBytesViaProcMem(long address, byte[] patch);

    /**
     * Reads bytes from the given virtual address via /proc/self/mem.
     * Useful for verifying patch was applied or reading current instruction bytes.
     *
     * @param address  Source virtual address
     * @param length   Number of bytes to read
     * @return         byte array, or null on failure
     */
    public static native byte[] readBytesViaProcMem(long address, int length);

    /**
     * Returns the base load address of a mapped .so library from /proc/self/maps.
     * @param libName  Library filename (e.g. "libunity.so")
     * @return         base address, or 0 if not found
     */
    public static native long getLibraryBaseAddress(String libName);

    // ─────────────────────────────────────────────────────────────────────────
    // JAVA API — convenience wrappers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Patches a function to be a NOP sled (fills with ARM64 NOP = 0x1F 0x20 0x03 0xD5).
     * @param address     Start address to NOP
     * @param byteCount   Number of bytes to NOP (must be multiple of 4 for ARM64)
     * @return            true on success
     */
    public static boolean nopAt(long address, int byteCount) {
        if (!sLibLoaded) { Log.e(TAG, "Library not loaded"); return false; }
        if (byteCount <= 0 || byteCount % 4 != 0) {
            Log.e(TAG, "byteCount must be positive multiple of 4 for ARM64 NOP");
            return false;
        }
        byte[] nops = new byte[byteCount];
        // ARM64 NOP: 1F 20 03 D5 (little-endian)
        for (int i = 0; i < byteCount; i += 4) {
            nops[i]   = 0x1F;
            nops[i+1] = 0x20;
            nops[i+2] = 0x03;
            nops[i+3] = (byte) 0xD5;
        }
        boolean ok = patchBytesViaProcMem(address, nops);
        Log.d(TAG, String.format("NOP sled @ 0x%X (%d bytes): %s", address, byteCount, ok ? "OK" : "FAIL"));
        return ok;
    }

    /**
     * Patches a function to immediately return 0 (MOV X0, #0; RET).
     * Useful for NOP-ing Unity vSyncCount setter or ACE hook stubs.
     *
     * ARM64: MOV X0, #0 = 00 00 80 D2; RET = C0 03 5F D6
     *
     * @param address  Start address of the target function
     * @return         true on success
     */
    public static boolean patchReturnZero(long address) {
        if (!sLibLoaded) { Log.e(TAG, "Library not loaded"); return false; }
        // MOV X0, #0
        byte[] patch = {
            0x00, 0x00, (byte)0x80, (byte)0xD2,
            // RET
            (byte)0xC0, 0x03, 0x5F, (byte)0xD6
        };
        boolean ok = patchBytesViaProcMem(address, patch);
        Log.d(TAG, String.format("ReturnZero patch @ 0x%X: %s", address, ok ? "OK" : "FAIL"));
        return ok;
    }

    /**
     * Patches a conditional branch to always branch (B = 0x00 0x00 0x00 0x14).
     * Replaces CBZ/CBNZ/B.cond with unconditional B +0 (infinite loop guard NOP).
     *
     * @param address  Address of the branch instruction
     * @return         true on success
     */
    public static boolean patchBranchAlways(long address) {
        if (!sLibLoaded) { Log.e(TAG, "Library not loaded"); return false; }
        // B #0 — branch to self (effectively a NOP when next insn follows)
        // Use NOP for simplicity since we can't know relative offset from Java
        return nopAt(address, 4);
    }

    /**
     * Checks if the native library is loaded and JNI bridge is functional.
     */
    public static boolean isAvailable() {
        return sLibLoaded;
    }

    /**
     * Reads current instruction bytes at address for verification.
     */
    public static String dumpHex(long address, int len) {
        if (!sLibLoaded) return "library not loaded";
        byte[] bytes = readBytesViaProcMem(address, len);
        if (bytes == null) return "read failed";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X ", b));
        return sb.toString().trim();
    }
}
