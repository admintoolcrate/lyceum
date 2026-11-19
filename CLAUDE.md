# Lyceum: working conventions

Lyceum is a quiet daily reflection journal for Android. These rules hold for every change.

## Principles (non-negotiable)
- **Free, no ads, no accounts, no analytics, no crash reporters, no trackers.** Do not add any SDK that phones home.
- **Fully offline.** The only network code is the *optional* Google Drive backup (`backup/drive`), which
  runs only when the user turns it on. `INTERNET` is used by nothing else.
- **No gamification.** No streaks, counts of days written, badges, scores, confetti or progress rings.
  Moods are words, never charted as a score.
- **No guilt copy.** Reminders and empty states are gentle and never mention missed days.
  Write "whenever you like", not "Don't break your streak!"
- Out of scope: social features, AI analysis, cloud accounts.

## Look
- Paper palette in `ui/theme/Color.kt` (`PaperLight`, `PaperDark`). Use `LyceumTheme.paper` tokens
  (`page`, `card`, `ink`, `muted`, `hairline`, `accent`, `accentSoft`). Never hard-code colours in screens.
- EB Garamond (`Garamond`) for headings, dates, prompts and quotations; Inter (`Inter`) for body and labels.
- Hairline dividers instead of shadows, no tonal elevation, generous margins (24 dp page padding).
- Both light and dark must look right; check both when adding UI.

## Stack and layout
Kotlin, Jetpack Compose, Material 3, Room, Hilt, WorkManager, DataStore. Min SDK 26, target/compile 36, JVM 17.
Versions live only in `gradle/libs.versions.toml`.

```
cc.toolcrate.lyceum
  data/db        Room database, entities, DAOs, FTS
  data/repo      repositories (the only thing ViewModels talk to for data)
  data/settings  SettingsStore (DataStore Preferences)
  domain         pure Kotlin logic: no Android imports, always unit-tested
  quotes         Aristotle quotations and themes
  reminder       evening reminder (WorkManager)
  security       app lock: PIN hashing, backoff, biometric
  backup         JSON export/import, encryption envelope, Drive
  stagira        intent contract with the Stagira app (docs/stagira-intent.md)
  ui/theme       palette, type, shapes
  ui/<screen>    Screen.kt + ViewModel.kt per screen
  di             Hilt modules
```
- MVVM: a screen collects a `StateFlow<UiState>` from its `@HiltViewModel`; events go up as lambdas.
- Logic that can be pure goes in `domain/` and gets a JVM unit test.
- Inject a `java.time.Clock` instead of calling `LocalDate.now()` in logic, so it can be tested.

## Data
- Room schemas are exported to `app/schemas/`; commit them. Every schema change bumps the version and
  adds a real `Migration` with a test. **Never** use `fallbackToDestructiveMigration`; these are people's journals.
- One entry per calendar day (`entries.date` is a unique epoch day).
- Import and restore merge day by day: the page with the newer `updatedAt` wins. Nothing local is deleted.
- The JSON export format (`backup/JournalCodec`) is versioned; keep reading every older version.

## Build, test, commit
- Build: `./gradlew assembleDebug`. Tests: `./gradlew testDebugUnitTest`.
- `./gradlew assembleDebug` **must pass before every commit**; run the unit tests too.
- Data-layer tests use Robolectric with an in-memory Room database (`app/src/test`).
- `ui/RenderTest` draws real screens in light and dark to `app/build/screens/`; look at them after UI changes.
- Builds fetch from Google's Maven Central mirror first (see `settings.gradle.kts`); Robolectric uses it too.
- Commit after each milestone with a plain, descriptive message. Never put model names or identifiers in
  commits, code or docs.
- `local.properties` (SDK path) and release signing files are never committed. `app/debug.keystore` is a
  deliberate, debug-only exception so the Drive OAuth client sees a stable SHA-1 (docs/drive-setup.md).
