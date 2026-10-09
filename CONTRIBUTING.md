# Contributing

Thanks for helping. A few notes keep changes easy to review.

## Reporting a problem

Open an issue and include:

- Android version and device (Motorola model)
- Framework and version (Vector, LSPosed, and so on)
- Whether `com.motorola.ccc.ota` still runs or notifies after enabling the module and rebooting
- A filtered logcat: `adb logcat -s OtaKiller`

## Changing code

- The notification-enqueue hook must fail closed: match the method by name on the live class, and skip installing it rather than guessing a signature if there's no unique match.
- Never let a hook exception propagate into `system_server` or the target app. Catch, log once, and fall through to the original behavior.
- Keep the module small and single-purpose: no UI, no extra permissions, no telemetry.

## Pull requests

Keep them focused, explain what you verified and on which Android version/device, and update `CHANGELOG.md` for user-visible changes.
