# Architecture

Game Launcher Pro is a single-module Android app written in Java. The package layout is organised by feature so a screen, its supporting models, and its Android integrations remain close together.

```text
android/app/src/main/
├── java/com/gamebooster/app/
│   ├── core/                 # Small shared, feature-neutral utilities
│   ├── engine/               # Display and system-property orchestration
│   ├── platform/             # Android platform entry points and privileged bridges
│   ├── feature/              # User-facing feature packages
│   │   ├── dashboard/        # Web dashboard and its JavaScript bridge
│   │   ├── games/            # Library, scanning, and game-space automation
│   │   ├── gameprofiles/     # Per-game profiles, patchers, and configuration
│   │   ├── performance/      # Boosting, refresh-rate, thermal, and scheduling
│   │   ├── settings/         # Settings presentation and preferences
│   │   ├── spoofer/          # Device-profile presentation and application
│   │   └── ...
│   ├── shizuku/              # Shizuku lifecycle and user-service integration
│   └── ui/common/            # Reusable UI-only components
├── res/                      # Android resources used by native screens
├── assets/                   # WebView files and JSON configuration
└── aidl/                     # Shizuku user-service contract
```

## Rules

- Put code in the feature that owns the behavior; do not add new files to a generic `config` or `ui/screens` package.
- Keep `core` feature-neutral. A class that knows about a screen, a WebView, a game, or a system service belongs elsewhere.
- Keep Android components (`Activity`, `Service`, `BroadcastReceiver`, and `TileService`) in a package that reflects both their platform role and owning feature.
- Keep JavaScript bridges beside the WebView feature, not in `core`.
- Keep game-specific configuration patchers in `feature.gameprofiles.patcher` and their preferences/models in their matching subpackages.
- Add pure-logic tests under `src/test/java` and device/integration tests under `src/androidTest/java` with the same package path as production code.

## Migration approach

Package moves must update imports and AndroidManifest component names together. Make moves feature-by-feature, build with `./gradlew :app:assembleDebug`, and avoid mixing a behavior change with a package-only refactor.
