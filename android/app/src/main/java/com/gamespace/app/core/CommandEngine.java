package com.gamespace.app.core;

import android.content.Context;

import com.gamespace.app.utils.ShellExecutor;
import com.gamespace.app.utils.ShizukuExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class CommandEngine {

    public enum Method {
        ROOT("Root (su)"),
        SHIZUKU("Shizuku/ADB"),
        ADB_LOCAL("Local Shell (adb/termux)"),
        SYSTEM_API("Android Settings API");

        public final String displayName;
        Method(String displayName) { this.displayName = displayName; }
    }

    public enum Priority { LOW, NORMAL, HIGH, CRITICAL }

    public static class Command {
        public final String command;
        public final boolean requiresRoot;
        public final Priority priority;
        public final long timeoutMs;
        public final String description;

        private Command(Builder b) {
            this.command = b.command;
            this.requiresRoot = b.requiresRoot;
            this.priority = b.priority;
            this.timeoutMs = b.timeoutMs;
            this.description = b.description;
        }

        public static Builder builder(String command) { return new Builder(command); }

        public static class Builder {
            private final String command;
            private boolean requiresRoot = false;
            private Priority priority = Priority.NORMAL;
            private long timeoutMs = 10000;
            private String description = "";

            Builder(String command) { this.command = command; }
            public Builder requiresRoot(boolean v) { requiresRoot = v; return this; }
            public Builder priority(Priority v) { priority = v; return this; }
            public Builder timeoutMs(long v) { timeoutMs = v; return this; }
            public Builder description(String v) { description = v; return this; }
            public Command build() { return new Command(this); }
        }
    }

    public static class Result {
        public final boolean success;
        public final String stdout;
        public final String stderr;
        public final Method methodUsed;
        public final long durationMs;
        public final int exitCode;

        Result(boolean success, String stdout, String stderr, Method methodUsed, long durationMs, int exitCode) {
            this.success = success;
            this.stdout = stdout;
            this.stderr = stderr;
            this.methodUsed = methodUsed;
            this.durationMs = durationMs;
            this.exitCode = exitCode;
        }

        public String getOutput() { return stdout.isEmpty() ? stderr : stdout; }
    }

    public static class CapabilitySet {
        public final boolean hasRoot;
        public final boolean hasShizuku;
        public final boolean hasAdbLocal;
        public final boolean hasSystemApi;
        public final Method bestMethod;

        CapabilitySet(boolean root, boolean shizuku, boolean adb, boolean sysApi) {
            this.hasRoot = root;
            this.hasShizuku = shizuku;
            this.hasAdbLocal = adb;
            this.hasSystemApi = sysApi;
            this.bestMethod = root ? Method.ROOT : shizuku ? Method.SHIZUKU : adb ? Method.ADB_LOCAL : Method.SYSTEM_API;
        }
    }

    public interface ExecutorStrategy {
        Method getMethod();
        boolean isAvailable();
        Result execute(Command cmd);
        boolean supports(Command cmd);
    }

    private static final class RootExecutor implements ExecutorStrategy {
        @Override public Method getMethod() { return Method.ROOT; }
        @Override public boolean isAvailable() { return ShellExecutor.isRootAvailable(); }
        @Override public boolean supports(Command cmd) { return isAvailable() && cmd.requiresRoot; }
        @Override public Result execute(Command cmd) {
            long start = System.currentTimeMillis();
            ShellExecutor.CommandResult r = ShellExecutor.executeCommand(cmd.command, true);
            return new Result(r.isSuccess(), r.stdout, r.stderr, Method.ROOT, System.currentTimeMillis() - start, r.exitCode);
        }
    }

    private static final class ShizukuExecutorStrategy implements ExecutorStrategy {
        @Override public Method getMethod() { return Method.SHIZUKU; }
        @Override public boolean isAvailable() { return ShizukuExecutor.hasShizukuPermission(); }
        @Override public boolean supports(Command cmd) { return isAvailable() && !cmd.requiresRoot; }
        @Override public Result execute(Command cmd) {
            long start = System.currentTimeMillis();
            String out = ShizukuExecutor.executeShizukuCommand(cmd.command);
            boolean ok = !out.startsWith("ERROR");
            return new Result(ok, out, ok ? "" : out, Method.SHIZUKU, System.currentTimeMillis() - start, ok ? 0 : -1);
        }
    }

    private static final class AdbLocalExecutor implements ExecutorStrategy {
        @Override public Method getMethod() { return Method.ADB_LOCAL; }
        @Override public boolean isAvailable() { return true; }
        @Override public boolean supports(Command cmd) { return !cmd.requiresRoot; }
        @Override public Result execute(Command cmd) {
            long start = System.currentTimeMillis();
            ShellExecutor.CommandResult r = ShellExecutor.executeCommand(cmd.command, false);
            return new Result(r.isSuccess(), r.stdout, r.stderr, Method.ADB_LOCAL, System.currentTimeMillis() - start, r.exitCode);
        }
    }

    private static final class SystemApiExecutor implements ExecutorStrategy {
        private final Context context;
        SystemApiExecutor(Context ctx) { this.context = ctx; }
        @Override public Method getMethod() { return Method.SYSTEM_API; }
        @Override public boolean isAvailable() { return context != null; }
        @Override public boolean supports(Command cmd) {
            return isAvailable() && (cmd.command.startsWith("settings put ") || cmd.command.startsWith("settings get "));
        }
        @Override public Result execute(Command cmd) {
            long start = System.currentTimeMillis();
            try {
                String[] parts = cmd.command.split(" ", 4);
                if (parts.length >= 4 && parts[0].equals("settings") && parts[1].equals("put")) {
                    String ns = parts[2];
                    String[] kv = parts[3].split(" ", 2);
                    if (kv.length == 2) {
                        android.provider.Settings.System.putString(context.getContentResolver(), kv[0], kv[1]);
                        return new Result(true, "OK", "", Method.SYSTEM_API, System.currentTimeMillis() - start, 0);
                    }
                }
                return new Result(false, "", "Unsupported system API command", Method.SYSTEM_API, System.currentTimeMillis() - start, -1);
            } catch (Exception e) {
                return new Result(false, "", e.getMessage(), Method.SYSTEM_API, System.currentTimeMillis() - start, -1);
            }
        }
    }

    private final List<ExecutorStrategy> strategies;
    private final ExecutorService executorService;
    private final ConcurrentHashMap<String, Result> cache;
    private final Context context;

    private CommandEngine(Context ctx, List<ExecutorStrategy> strategies) {
        this.context = ctx;
        this.strategies = strategies;
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "CommandEngine");
            t.setPriority(Thread.NORM_PRIORITY - 1);
            return t;
        });
        this.cache = new ConcurrentHashMap<>();
    }

    public static CommandEngine configure(Context ctx) {
        List<ExecutorStrategy> list = new ArrayList<>();
        list.add(new RootExecutor());
        list.add(new ShizukuExecutorStrategy());
        list.add(new AdbLocalExecutor());
        list.add(new SystemApiExecutor(ctx));
        return new CommandEngine(ctx, list);
    }

    public Result execute(Command cmd) {
        String cacheKey = cmd.command + ":" + cmd.requiresRoot;
        Result cached = cache.get(cacheKey);
        if (cached != null && cached.success) return cached;

        for (ExecutorStrategy s : strategies) {
            if (s.isAvailable() && s.supports(cmd)) {
                Result r = s.execute(cmd);
                if (r.success) cache.put(cacheKey, r);
                return r;
            }
        }
        return new Result(false, "", "No available executor for: " + cmd.command, Method.ADB_LOCAL, 0, -1);
    }

    public void executeAsync(Command cmd, java.util.function.Consumer<Result> callback) {
        executorService.submit(() -> callback.accept(execute(cmd)));
    }

    public List<Result> executeBatch(List<Command> cmds) {
        List<Result> results = new ArrayList<>(cmds.size());
        for (Command c : cmds) results.add(execute(c));
        return results;
    }

    public CapabilitySet getCapabilities() {
        return new CapabilitySet(
            ShellExecutor.isRootAvailable(),
            ShizukuExecutor.hasShizukuPermission(),
            true,
            context != null
        );
    }

    public void clearCache() { cache.clear(); }
    public void shutdown() { executorService.shutdown(); try { executorService.awaitTermination(5, TimeUnit.SECONDS); } catch (InterruptedException ignored) {} }
}