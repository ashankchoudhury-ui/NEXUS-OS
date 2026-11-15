# NEXUS — Android operating-layer prototype

NEXUS is a launcher and control surface built on top of the phone's existing Android installation. It is not a custom ROM or replacement operating system in this phase.

## Architecture and feasibility

Read the current technical specification in [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md). It covers the USER_SPACE / AGENT_SPACE model, Work Profile feasibility, Shizuku and Termux boundaries, resource-management limits, device audit commands, safety rules and staged implementation plan.

## Current implementation status

- The Android app is currently a WebView-based UI shell loading a local HTML asset.
- A HOME intent filter is declared, but default-launcher behaviour has not been validated on the target phone.
- There is no verified Work Profile integration, app-data isolation, live system telemetry, process control, Shizuku bridge or Termux/Hermes bridge yet.
- GitHub Actions compiles a debug APK and uploads it as an artifact. A successful CI build is not proof of on-device compatibility.

## Device compatibility

The target is the user's existing Lava phone. A previous device inspection reported Lava Z35, Android 14 / API 34, MT6765 and arm64, but those values have not been re-verified. RAM availability, `/data` free space, managed-profile support, bootloader state and recovery compatibility remain unconfirmed. No root, flashing, destructive debloating or bootloader unlocking is part of the current plan.

## Build

GitHub Actions runs `gradle assembleDebug` on pushes to `main` and uploads `app/build/outputs/apk/debug/app-debug.apk` as the `NEXUS-debug-apk` artifact.
