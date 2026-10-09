<div align="center">

# Moto OTA Killer

**Stops `com.motorola.ccc.ota` from running or notifying, permanently.**

An open-source Xposed module for Android 9 and up, by [DroidRooter](https://droidrooter.com).

[![License](https://img.shields.io/github/license/saadnahid7/MotoOtaKiller?style=for-the-badge&color=blue)](LICENSE)
[![Release](https://img.shields.io/github/v/release/saadnahid7/MotoOtaKiller?style=for-the-badge&color=teal)](https://github.com/saadnahid7/MotoOtaKiller/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/saadnahid7/MotoOtaKiller/total?style=for-the-badge&color=violet)](https://github.com/saadnahid7/MotoOtaKiller/releases)
[![Android](https://img.shields.io/badge/Android-9%20to%2017-3DDC84?style=for-the-badge&logo=android&logoColor=white)](#compatibility)
[![Xposed API](https://img.shields.io/badge/libxposed%20API-102-orange?style=for-the-badge)](https://github.com/libxposed/api)

[![DroidRooter](https://img.shields.io/badge/DroidRooter-droidrooter.com-2F6BFF?style=for-the-badge)](https://droidrooter.com)
[![Telegram](https://img.shields.io/badge/Telegram-@DroidRooter-2CA5E0?style=for-the-badge&logo=telegram&logoColor=white)](https://t.me/DroidRooter)

</div>

---

## Why this exists

Motorola's software-update client, `com.motorola.ccc.ota`, posts update reminders through notification channels that are marked `fixedImportance` at the framework level. That means:

- Disabling the app with `pm disable-user` does not stop it — something still relaunches its process in the background to push the notification.
- Demoting or blocking its channel from Settings, or from another Xposed module that only patches the app's own notification calls, does nothing, because the importance lock is enforced by `NotificationManagerService` in `system_server`, not by the app itself.

This module kills `com.motorola.ccc.ota`'s process the instant it starts, before it can do anything — including post a notification. No root shell command, Magisk module, or app-side Xposed hook can reach this chokepoint; this one hooks the module loader itself.

## How it works

- **Kill on sight.** The moment the target package's classloader is ready (meaning its process just started), the process is killed outright. No UI, no delay, no reliance on `pm disable`.
- **System-layer block (where supported).** On frameworks that allow module injection into `system_server`, it also hooks `NotificationManagerService`'s notification-enqueue entry point and silently drops anything posted under the target package name, as a second layer independent of whether the app's own process is even alive.

> [!NOTE]
> Whether the system-layer hook actually loads depends on your framework build allowing system-server injection for third-party modules — some Zygisk-based frameworks disable this by default for stability. The kill-on-sight hook does not depend on it and works regardless.

## Requirements

- A rooted device with an LSPosed-compatible framework that supports the modern **libxposed API 102**, such as [Vector](https://github.com/JingMatrix/Vector) or a recent [LSPosed](https://github.com/LSPosed/LSPosed).
- A Motorola device shipping `com.motorola.ccc.ota` (most Moto phones since Android 9).

> [!IMPORTANT]
> This module is built on the modern libxposed API. Legacy Xposed and EdXposed are not supported.

## Installation

1. Download the latest APK from [Releases](https://github.com/saadnahid7/MotoOtaKiller/releases/latest) and install it.
2. Open Vector or LSPosed, enable **Moto OTA Killer**, and scope it to:
   - `com.motorola.ccc.ota`
   - **System Framework** (`android`/`system`), if your framework exposes it — optional, see note above.
3. Restart the device once so the hooks load.

There is nothing to open afterward — this module has no UI and no launcher icon by design. It runs silently in the background.

## Compatibility

| Android | API | Status |
|---|---|---|
| 16 | 36 | Verified (Moto G 2026, `nevada_g_sysu`) |

Verified means the target package's process was observed being killed within milliseconds of starting, confirmed via logcat on the device and Android version listed. Other Motorola devices and Android versions should work identically since the hook targets a package name, not device-specific code, but are unverified. Please open an issue with your device and Android version if you test one.

## Limits

- The system-layer notification block does not install on frameworks that don't allow system-server injection for modules (see note above). The kill-on-sight hook is unaffected by this and is the primary mechanism.
- If Motorola ever renames the package id, this module will need an update to match.
- This only targets `com.motorola.ccc.ota`. It does not touch any other Motorola service.

## Building

You need JDK 17 or newer and the Android SDK with platform 36.

```bash
./gradlew :app:assembleRelease
```

The release build is unsigned unless `.local/signing.properties` exists.

## Project layout

```
app/
  src/main/kotlin/com/droidrooter/otakiller/
    HookEntry.kt   module entry point: kill-on-sight + system_server notification block
  src/main/resources/META-INF/xposed/   libxposed module metadata (scope, entry class)
```

## Open source

Moto OTA Killer is free and open source under the [MIT license](LICENSE). There is no closed part, no tracking, no account, and no UI to build trust around — read the one hook file, it does exactly what this README says.

## Contributing

Issues and pull requests are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) first.

## Disclaimer

This module is for owners of their own Motorola device who want to stop an unwanted background service. You are responsible for how you use it, including any effect on receiving genuine software updates you may still want. The software is provided as is, without warranty.

## Acknowledgements

- [libxposed](https://github.com/libxposed) for the modern Xposed API.
- [LSPosed](https://github.com/LSPosed/LSPosed) and [Vector](https://github.com/JingMatrix/Vector) for the frameworks it runs on.

## From DroidRooter

Moto OTA Killer is a project of **DroidRooter**, a small team building tools for rooted Android.

- **Website:** [droidrooter.com](https://droidrooter.com)
- **Telegram:** [@DroidRooter](https://t.me/DroidRooter)
- **DRVCAM, DroidRooter's Virtual Camera:** [droidrooter.com/drvcam](https://www.droidrooter.com/drvcam/) feeds your own video or images to apps in place of the camera, on rooted Android 9 to 17.

## License

[MIT](LICENSE) © DroidRooter
