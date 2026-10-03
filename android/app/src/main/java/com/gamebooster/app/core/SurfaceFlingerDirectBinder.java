package com.gamebooster.app.core;

import android.os.IBinder;
import android.os.Parcel;
import android.util.Log;

import com.gamebooster.app.engine.PrivilegeBridgeEngine;

import java.lang.reflect.Method;

/**
 * SurfaceFlingerDirectBinder — Low-overhead Binder IPC interface for Android SurfaceFlinger.
 *
 * Bypasses fork/exec shell overhead by executing binder transactions directly via
 * reflection against android.os.ServiceManager, with automatic fallback through
 * PrivilegeBridgeEngine (Root/Shizuku) if local process UID lacks direct SELinux domain rights.
 */
public final class SurfaceFlingerDirectBinder {

    private static final String TAG = "SFDirectBinder";
    private static final String DESCRIPTOR = "android.ui.ISurfaceComposer";

    // SurfaceFlinger transaction codes
    public static final int TRANSACTION_SET_REFRESH_RATE_OVERRIDE = 1035;
    public static final int TRANSACTION_SET_FPS_LIMIT = 1036;
    public static final int TRANSACTION_FLUSH_COMPOSER = 1008;

    private static volatile IBinder sCachedSurfaceFlingerBinder = null;

    private SurfaceFlingerDirectBinder() {}

    /**
     * Obtains the raw SurfaceFlinger IBinder reference from ServiceManager.
     */
    private static IBinder getSurfaceFlingerService() {
        if (sCachedSurfaceFlingerBinder != null && sCachedSurfaceFlingerBinder.isBinderAlive()) {
            return sCachedSurfaceFlingerBinder;
        }

        try {
            Class<?> smClass = Class.forName("android.os.ServiceManager");
            Method getService = smClass.getMethod("getService", String.class);
            IBinder binder = (IBinder) getService.invoke(null, "SurfaceFlinger");
            if (binder != null && binder.isBinderAlive()) {
                sCachedSurfaceFlingerBinder = binder;
                return binder;
            }
        } catch (Throwable t) {
            Log.d(TAG, "Direct ServiceManager lookup note: " + t.getMessage());
        }
        return null;
    }

    /**
     * Executes a direct SurfaceFlinger binder transaction.
     * If local UID is restricted, falls back through PrivilegeBridgeEngine (Root/Shizuku).
     */
    public static boolean transactDirect(int code, int param) {
        IBinder binder = getSurfaceFlingerService();
        if (binder != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                data.writeInt(param);
                boolean ok = binder.transact(code, data, reply, 0);
                reply.readException();
                if (ok) {
                    Log.d(TAG, "Direct SurfaceFlinger transact succeeded: code=" + code + ", param=" + param);
                    return true;
                }
            } catch (SecurityException se) {
                Log.d(TAG, "In-process transact security restriction: " + se.getMessage() + ", falling back to privileged bridge");
            } catch (Throwable t) {
                Log.d(TAG, "In-process transact exception: " + t.getMessage());
            } finally {
                data.recycle();
                reply.recycle();
            }
        }

        // Privileged fallback via virtual root / Shizuku
        String cmd = "service call SurfaceFlinger " + code + " i32 " + param;
        String res = PrivilegeBridgeEngine.executePrivileged(cmd);
        return res != null && !res.startsWith("ERROR") && !res.isEmpty();
    }

    /**
     * Enforces the target refresh rate directly into SurfaceFlinger display composition pipeline.
     */
    public static boolean setRefreshRate(int targetHz) {
        boolean r1 = transactDirect(TRANSACTION_SET_REFRESH_RATE_OVERRIDE, targetHz);
        boolean r2 = transactDirect(TRANSACTION_SET_FPS_LIMIT, targetHz);
        return r1 || r2;
    }

    /**
     * Flushes SurfaceFlinger composer layer cache to prevent old frame buffering.
     */
    public static boolean flushComposerCache() {
        IBinder binder = getSurfaceFlingerService();
        if (binder != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                boolean ok = binder.transact(TRANSACTION_FLUSH_COMPOSER, data, reply, 0);
                if (ok) return true;
            } catch (Throwable ignored) {
            } finally {
                data.recycle();
                reply.recycle();
            }
        }

        String res = PrivilegeBridgeEngine.executePrivileged("service call SurfaceFlinger " + TRANSACTION_FLUSH_COMPOSER + " 2>/dev/null");
        return res != null && !res.startsWith("ERROR");
    }
}
