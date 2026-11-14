# NEXUS — Android Prototype (Build 001)

Native Android UI prototype for the NEXUS operating-layer concept.

## Current scope
- Home, Spaces, Processes, Terminal, and System screens.
- Burgundy / ivory visual system with a dark-mode toggle.
- GitHub Actions workflow to compile a debug APK.

## Limitations
This build is UI only. It does not create Android user/profile isolation, kill or freeze apps, read live system metrics, control CPU scheduling, or connect to Termux/Hermes. Displayed values are placeholders, not live telemetry.

## Build
GitHub Actions runs `gradle assembleDebug` on pushes to `main` and uploads the debug APK as an artifact.

## Target
Initial target: Lava Z35, Android 14, arm64. Device compatibility has not yet been tested.
