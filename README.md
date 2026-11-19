# Lyceum

A quiet daily reflection journal for Android.

One short prompt a day. Write a few lines, note your mood if you like, and one thing you learned.
After you save, a short passage from Aristotle chosen to suit the day.

- Free, no ads, no accounts, no analytics. Everything stays on your phone.
- No streaks, no scores, no guilt.
- Calendar, search, tags and a weekly review.
- Optional gentle evening reminder.
- App lock (PIN or biometric), JSON export/import.
- Optional passphrase-encrypted backup to your own Google Drive (hidden app folder).
- Optional link to a goal in [Stagira](https://github.com/admintoolcrate/Stagira).

## Building

Requires JDK 17+ and the Android SDK (platform 36).

```bash
./gradlew assembleDebug
```

```bash
./gradlew testDebugUnitTest
```

See `CLAUDE.md` for project conventions and `third_party/README.md` for bundled fonts and texts.
