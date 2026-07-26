# 🤖 Agent Skills Integration — GAME SPACE

This document outlines the specialized **Agent Skills** integrated into the development, quality assurance, static analysis, and automated testing workflows of **GAME SPACE**.

---

## 🛠️ Registered Agent Skills

### 1. `android-cli`
- **Purpose**: Orchestrates Android development tasks including SDK management (`cmdline-tools`, `platform-tools`), Gradle compilation, environment diagnostics, and APK deployment.
- **Usage**: Used during release builds, JDK 17 environment verification, and device ADB installation.

### 2. `dart-run-static-analysis`
- **Purpose**: Strict static analysis enforcement, type safety verification (`strict-casts`, `strict-inference`, `strict-raw-types`), and linter diagnostics.
- **Usage**: Enforces code quality rules across configuration files (`analysis_options.yaml`).

### 3. `dart-add-unit-test`
- **Purpose**: Unit test suite generation and regression testing for application state managers, permissions entities, and refresh rate locking.
- **Usage**: Provides automated unit tests in `test/` to verify state mutations.

### 4. `memory-leak-debugging`
- **Purpose**: Diagnoses and resolves Android memory leaks, background process overhead, and unclosed stream readers.
- **Usage**: Used in `ShizukuExecutor.java` and `ShellExecutor.java` to ensure proper process destruction and stream closing (`BufferedReader.close()`, `Process.destroy()`).

### 5. `troubleshooting`
- **Purpose**: Rapid error root-cause diagnosis, NDK path resolution, Java version mismatch detection, and permission issue resolution.
- **Usage**: Resolves NDK missing `source.properties` errors and `/usr/lib/android-sdk` write permission issues.
