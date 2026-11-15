#!/data/data/com.termux/files/usr/bin/bash
# NEXUS read-only device audit. No settings, packages, profiles or bootloader state are changed.
# Run from Termux: bash nexus-device-audit.sh

set +e

section() { printf '\n===== %s =====\n' "$1"; }
prop() {
  local key="$1" value
  value="$(getprop "$key" 2>/dev/null)"
  if [ -n "$value" ]; then
    printf '%s=%s\n' "$key" "$value"
  else
    printf '%s=<blank or unavailable>\n' "$key"
  fi
}

section "DEVICE IDENTITY"
for key in \
  ro.product.manufacturer \
  ro.product.model \
  ro.product.device \
  ro.build.version.release \
  ro.build.version.sdk \
  ro.build.display.id \
  ro.hardware \
  ro.soc.model \
  ro.product.cpu.abilist; do
  prop "$key"
done
printf 'kernel='
uname -sr 2>/dev/null || echo "<unavailable>"

section "MEMORY"
if [ -r /proc/meminfo ]; then
  grep -E '^(MemTotal|MemAvailable|SwapTotal|SwapFree):' /proc/meminfo
else
  echo "/proc/meminfo unavailable"
fi

section "STORAGE"
df -h /data 2>&1
df -h /storage/emulated/0 2>&1

section "DISPLAY"
if command -v wm >/dev/null 2>&1; then
  wm size 2>&1
  wm density 2>&1
else
  echo "wm command unavailable in this shell"
fi

section "PROFILE / DEVICE-MANAGEMENT CAPABILITY (READ ONLY)"
if command -v pm >/dev/null 2>&1; then
  echo "-- Relevant advertised package-manager features --"
  pm list features 2>&1 | grep -Ei 'managed_users|device_admin|multiuser|users' || echo "No matching feature line returned (not proof of unsupported hardware)."
  echo "-- Android users/profiles --"
  pm list users 2>&1
else
  echo "pm command unavailable in this shell"
fi

section "BOOT / ENCRYPTION HINTS (NOT A BOOTLOADER UNLOCK TEST)"
for key in \
  ro.boot.flash.locked \
  ro.boot.verifiedbootstate \
  ro.boot.vbmeta.device_state \
  ro.crypto.state \
  ro.crypto.type; do
  prop "$key"
done

section "INTERPRETATION"
cat <<'NOTE'
This audit only reads system properties, procfs, filesystem capacity and package-manager
information. It does not change settings, install/remove apps, create profiles, unlock
the bootloader, flash partitions or require root.

Blank boot properties are inconclusive. They do not prove that OEM unlocking is available.
A pm/Binder error is also inconclusive about managed-profile support. The definitive app-side
check is PackageManager.hasSystemFeature(PackageManager.FEATURE_MANAGED_USERS), followed by
checking whether Android's managed-profile provisioning flow is actually available.
Do not run any fastboot, flashing, reset or package-removal command as part of this audit.
NOTE
