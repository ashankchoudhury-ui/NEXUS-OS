# NEXUS — Android operating-layer prototype

**Architecture status:** feasibility-first specification  
**Date:** 2026-11-15  
**Target:** the user's existing Lava Android phone; exact firmware capabilities are not yet verified.

## 1. Product definition

NEXUS is **not a replacement Android OS or custom ROM** in this phase. It is a launcher and control surface that sits on top of Android. Android continues to own the kernel, app sandboxing, package manager, System UI, telephony, notifications, power management and security model.

The intended experience has two real Android environments where the device permits them:

- **USER_SPACE:** the phone's primary Android user, containing personal apps and data.
- **AGENT_SPACE:** one Android managed/work profile, containing agent-facing apps, a separate Termux installation if needed, and separate app data.

NEXUS provides the visual workspace switcher and profile-aware app entry points. A switch in the NEXUS UI must not be presented as security isolation unless Android has actually created a second profile. If managed profiles are unavailable, NEXUS may offer *logical workspaces* inside its own app, but must label them as UI-only and must not claim that third-party app data is separated.

Do not build a full Android VM, a second OS image, or a custom ROM for this hardware target. That is not a realistic first prototype on a low-memory phone and would add boot/recovery risk without solving the main use case.

## 2. Evidence, assumptions and unknowns

### Previously reported by the user — not re-verified in this run

A previous Termux/device-inspection output reported:

| Item | Previously reported value |
|---|---|
| Manufacturer / model | LAVA / Z35 |
| Hardware property | `mt6765` |
| Android release / SDK | Android 14 / API 34 |
| ABI | `arm64-v8a` |
| `MemTotal` | 3,911,944 kB (about 3.73 GiB) |
| `MemAvailable` at that moment | 1,061,028 kB (about 1.01 GiB) |
| Build fingerprint string | `LAVA_Z35_SW_U_V12_HW_V1.0_20260907` |
| Storage command | 49 GB total, 41 GB used, 7.6 GB free |

The storage result was obtained for a path mounted at `/storage/emulated/0/Android/obb`; it **does not establish free space on the actual `/data` filesystem**. Available RAM is a momentary reading, not a fixed device capacity.

The previous `pm list users` attempt failed with a Binder transaction error. That failure proves neither support nor lack of support for managed profiles.

### Must be inspected before profile setup or any privileged operation

- Exact model, build number, Android release/API level, kernel, SoC and supported ABIs.
- Actual `/data` free space and current memory headroom.
- Whether the system advertises `PackageManager.FEATURE_MANAGED_USERS` and permits managed-profile provisioning.
- Whether wireless debugging is present and stable enough for optional Shizuku startup.
- Bootloader lock state, OEM-unlock availability, exact stock firmware package and a proven recovery route. These are all **unknown**. An OEM-unlocking toggle, if present, is not proof that unlocking is possible.
- Whether the OEM's power manager aggressively kills background services.
- Actual screen dimensions/density and whether the current app behaves correctly as a default launcher.

No rooting, bootloader unlocking, flashing, factory reset, system-app removal or profile provisioning should be attempted until the relevant capability and recovery checks are complete.

## 3. Recommended architecture

Keep the first implementation small. Use a single Android app initially, with clear internal boundaries; split Gradle modules only when a feature is stable.

| Component | Responsibility | Android mechanism |
|---|---|---|
| **NEXUS Launcher** | Home screen, app index, workspace UI, settings and visual system | Native Kotlin; Jetpack Compose is the preferred long-term UI |
| **Workspace coordinator** | Knows the current workspace and maps it to a real Android user/profile when available | `UserManager`, `LauncherApps`, profile-aware intents |
| **Profile adapter** | Detects and, in a later phase, manages AGENT_SPACE | `DevicePolicyManager` / managed-profile DPC |
| **System observer** | Read-only memory, storage, battery, network and app-usage summaries | `ActivityManager`, `StatFs`, `BatteryManager`, `TrafficStats`, `UsageStatsManager` |
| **Termux bridge** | Starts approved local scripts and shows bounded output | Termux `RUN_COMMAND` intent / optional Termux:Tasker |
| **Shizuku adapter** | Optional, explicit shell-level operations on supported devices | Shizuku API; no-root mode only |
| **Hermes gateway** | Remote agent status and approved actions | HTTPS API or SSH to the existing VPS; never a local Android daemon pretending to be Hermes |

### Workspace mapping

1. Keep USER_SPACE as Android's primary user.
2. If managed-profile support is confirmed, map AGENT_SPACE to the single Work Profile. Install separate copies of the required apps inside it; Android keeps their app data separate.
3. Test profile-aware app discovery and launch on the actual OEM build. Intents and file paths do not freely cross profile boundaries. Prefer explicit content URIs with granted access when a user deliberately shares a file.
4. Do not promise a third isolated TEST_SPACE. A full secondary Android user is a separate, heavier facility and may be disabled by the OEM. Defer it until the phone proves that it supports it and has enough memory/storage headroom.
5. If a Work Profile cannot be created, offer a clearly labelled logical workspace in NEXUS only. That can separate NEXUS settings and shortcuts, not another app's private data.

Android's DPC documentation specifically says to check `FEATURE_MANAGED_USERS` before attempting managed-profile creation. The Android Enterprise model also documents separate app/data containers and profile-boundary restrictions:
- https://developer.android.com/work/dpc/build-dpc
- https://developer.android.com/work/managed-profiles
- https://developer.android.com/work/guide

## 4. What can and cannot be controlled without root

### Work Profile

**Can do, if supported and properly provisioned:**
- Keep separate app instances, accounts and app-private data for work-profile apps.
- Install and manage apps in the managed profile through its profile-owner/DPC.
- Pause the whole Work Profile. When paused, its apps stop running and its notifications stop too.
- Apply certain profile-scoped policies.

**Cannot safely promise:**
- Several arbitrary independent profiles on every Android device.
- Seamless launch of every app across profile boundaries without testing.
- That pausing AGENT_SPACE preserves notifications from apps inside it. It does not: paused work apps do not run or notify.
- That Work Profile gives NEXUS control over the primary profile's apps.

A managed profile is the strongest realistic route to genuine USER_SPACE/AGENT_SPACE separation without rooting. First test support with Android's documented feature check. Do not provision it blindly.

### Shizuku / ADB shell

Shizuku is an optional bridge for apps that explicitly implement its API. In no-root mode it uses ADB/shell-level privileges, **not unrestricted root**. On Android 11+ it can be started with Wireless debugging without a PC, but its service generally has to be started again after a reboot. OEM restrictions can still prevent or interrupt it.

Good first uses: read-only package inventory, diagnostics, and narrowly scoped reversible operations. Do not build a generic "run any shell command" button. Shizuku cannot grant access to another app's private sandbox or modify the kernel merely because it is installed.

- Official setup: https://shizuku.rikka.app/guide/setup/
- Project overview: https://shizuku.rikka.app/

### Termux and Linux

Termux provides a Linux userland running on Android's existing Linux kernel. It is not a separate Linux kernel, a full desktop Linux VM, or root access. It is suitable for Git, Python, SSH, scripts and VPS administration.

NEXUS can integrate with Termux's documented `RUN_COMMAND` service. This requires the caller's `com.termux.permission.RUN_COMMAND` permission and Termux's `allow-external-apps=true` setting. That bridge is powerful: a caller may receive command output and therefore access whatever the approved command can read. NEXUS must use a visible command allowlist, per-action confirmation for anything that changes state, bounded output, and an execution log. Do not pass arbitrary user text to a shell.

Install/use the Termux bridge in the same Android profile as the Termux instance it controls. Cross-profile intents are restricted by Android and must not be assumed to work.

- Official integration contract: https://github.com/termux/termux-app/wiki/RUN_COMMAND-Intent

### Process and resource management

Android already decides which cached processes to retain or kill. A normal third-party app cannot reliably freeze, kill or reprioritize every other app's process, change CPU governors, alter kernel scheduling, or reclaim RAM on demand.

NEXUS should therefore be an **observer and policy assistant**, not a fake RAM cleaner:

- Read memory/storage/battery/network snapshots when NEXUS is foregrounded.
- Request Usage Access only if app-usage history is needed, and explain why.
- Show stale/rarely used apps as candidates for the user to review.
- Offer links to Android's own per-app battery/background settings.
- If a DPC exists, expose profile-scoped suspension only as an explicit user action. Suspending a package suppresses its notifications; never auto-suspend messaging, alarms, accessibility, keyboard, launcher, VPN, Termux/SSH, or other user-designated critical apps.
- A single **Pause AGENT_SPACE** control may be offered with a clear warning that all work-profile apps and notifications stop while paused.

Android's process-lifecycle documentation confirms that cached-process management is owned by the system and process priority depends on active components and dependencies:
- https://developer.android.com/guide/components/activities/process-lifecycle
- https://developer.android.com/work/dpc/security

## 5. Hardware and performance policy

The previously reported memory size suggests a tight budget if it is accurate. Treat 4 GB-class RAM as a constraint until re-measured:

- No second Android VM, always-running agent runtime, heavy animated wallpaper, continuous process polling, or background dashboard loop.
- Keep the launcher process lean; load detailed charts only when opened.
- Use event-driven updates where possible and low-frequency foreground sampling otherwise.
- Avoid large image assets, blur-heavy layers, multiple WebViews and network font downloads.
- Cache only small, non-sensitive UI state. Do not duplicate personal files into AGENT_SPACE.
- Do not keep the screen awake during normal launcher use.
- Measure cold start, scrolling jank, memory footprint, idle battery drain and free `/data` before and after each feature.

## 6. Debloating policy

No mass-debloat script. First produce a package inventory and classify every candidate:

1. User-installed app that the user recognises.
2. Optional OEM app with a known replacement and tested rollback.
3. Core/system package — leave untouched.

Never disable/remove System UI, Settings, package installer, permission controller, WebView provider, launcher fallback, telephony, input method/keyboard, media provider, Google Play services (if present), network stack, updater or unknown vendor services. Removing a dependency can cause boot loops, broken notifications, setup failures or loss of app installation.

Start with Android Settings' own uninstall/disable controls. Only consider reversible per-user package changes after a complete inventory, backup and package-by-package test. Do not modify the system partition.

## 7. Security rules

- No root or device-owner mode for the whole phone in the prototype.
- No API keys, VPS passwords or private SSH keys hard-coded into the APK, HTML, Git repository or logs.
- Store local secrets with Android Keystore-backed encryption; keep Hermes credentials on the VPS where possible.
- Use HTTPS with certificate validation. For SSH, use keys and host-key verification; prefer a restricted VPS account/command surface.
- No arbitrary shell endpoint from the WebView. If the WebView remains, expose only narrow typed native actions and validate every argument.
- All package suspension, profile pause and destructive actions require an explicit user action and a clear explanation.
- Provide a visible emergency route back to the system launcher and a way to revoke Shizuku/notification/usage permissions.

## 8. Existing repository status and immediate code implications

The current `main` branch is a buildable **WebView shell**, not an Android operating layer yet:

- `MainActivity` loads `app/src/main/assets/index.html`.
- The manifest declares a HOME intent, but the activity is currently fixed to landscape.
- The activity hides system bars and sets `FLAG_KEEP_SCREEN_ON`; those are kiosk-like behaviours and should not be permanent defaults for a daily-use phone launcher.
- The HTML currently uses a green matrix-style palette and remote Google Fonts. The agreed NEXUS visual direction is pixel/bitmap details, monospaced technical labels, dense but legible layouts, and the ivory/charcoal or burgundy/charcoal palettes. The existing green page is not the final design reference.
- A GitHub Actions run completed successfully and produced a debug APK artifact. That proves the project compiles in CI; it does **not** prove it installs, works as HOME, fits the device, or has any profile/system-management capability.

Keep the current page as a visual prototype if useful. For real Android integrations, move system-facing features into native Kotlin components. Do not expose Shizuku or Termux command execution directly to JavaScript.

## 9. Staged implementation plan

### Stage 0 — Read-only device audit (next)

Collect exact build, API, ABI, memory, storage and profile capability. No setting changes, package removal or reboot required. The Termux commands are in the next-step checklist below. A small NEXUS diagnostics screen should additionally call `PackageManager.hasSystemFeature(PackageManager.FEATURE_MANAGED_USERS)` and report the result.

### Stage 1 — Stable launcher shell

- Make the UI portrait-first and adaptive; support rotation rather than forcing landscape.
- Remove always-on screen behaviour and make immersive mode optional.
- Implement app drawer, workspace switch UI, settings, accessible touch targets and crash-safe fallback.
- Do not claim real profile isolation yet.
- Verify that Android lets the user select NEXUS as the default Home app and switch back to the stock launcher.

### Stage 2 — Prove real isolation

- If `FEATURE_MANAGED_USERS` is true and provisioning is allowed, test a Work Profile using a reputable, verified DPC/profile manager.
- Verify separate app installation, accounts, storage, notifications and removal behaviour.
- Only after that test, decide whether NEXUS should include its own minimal DPC/profile-owner component.
- If unsupported, retain honest UI-only workspaces or investigate secondary-user support; do not fake isolation.

### Stage 3 — Termux/Hermes integration

- Begin with a read-only status command and a simple approved script.
- Add typed actions for VPS status, Git status and selected Hermes operations.
- Keep credentials on the VPS or in secure storage; require confirmation before any remote write/action.
- Test with Termux in AGENT_SPACE if the profile exists.

### Stage 4 — Resource observer and safe controls

- Add memory, storage, battery, network and app-usage summaries.
- Build a critical-app allowlist and a review queue for stale apps.
- Add explicit profile pause and profile-scoped app suspension only if DPC support is proven.
- No automatic force-stop/freeze/kill loop.

### Stage 5 — Optional Shizuku adapter and controlled debloat

- Test Wireless debugging and Shizuku separately.
- Add only documented, permission-checked capabilities with clear undo paths.
- Audit packages one by one. Measure actual performance before claiming improvement.

### Stage 6 — Device validation

Test install/update/uninstall, default launcher selection, reboot recovery, profile switching, notifications, calls, keyboard, battery, low-memory behaviour, Termux sessions and emergency fallback on the actual phone. A green CI check is not device validation.

## 10. Read-only next-step checklist

Run these in Termux and send the output. They do not require root:

```sh
echo "=== DEVICE ==="
getprop ro.product.manufacturer
getprop ro.product.model
getprop ro.product.device
getprop ro.build.version.release
getprop ro.build.version.sdk
getprop ro.build.display.id
getprop ro.hardware
getprop ro.soc.model
getprop ro.product.cpu.abilist

echo "=== MEMORY ==="
grep -E 'MemTotal|MemAvailable' /proc/meminfo

echo "=== DATA STORAGE ==="
df -h /data

echo "=== BOOT / ENCRYPTION HINTS ==="
getprop ro.boot.flash.locked
getprop ro.boot.verifiedbootstate
getprop ro.boot.vbmeta.device_state
getprop ro.crypto.state
getprop ro.crypto.type
```

Blank or vendor-specific boot properties are inconclusive; they do not establish that the bootloader can be unlocked. Do not run `fastboot flashing unlock` or any flashing command as part of this audit.

For Work Profile support, use a NEXUS diagnostic screen calling the Android API `PackageManager.FEATURE_MANAGED_USERS`; the previous `pm list users` Binder error is not a valid capability test. Also inspect Settings → Passwords & accounts (or search Settings for “Work profile”) for an existing work tab.

**Next engineering decision:** proceed with Stage 0 and keep the current APK as a UI-only shell until the device capability report is known. No root, flashing, destructive debloating or unverified APK installation is justified yet.
