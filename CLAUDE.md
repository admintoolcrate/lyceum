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
- Lyceum follows the ToolCrate family look shared with Stagira, Telos and Kairos:
  **read `docs/family-look.md`** (the same file in all four repos; change it in all four).
- Mono palette in `ui/theme/Color.kt` (`PaperLight`, `PaperDark`: black on white, true black at night;
  `accent` is ink). Use `LyceumTheme.paper` tokens (`page`, `card`, `ink`, `muted`, `hairline`, `accent`,
  `accentSoft`). Never hard-code colours in screens.
- EB Garamond (`Garamond`) for display moments only: the name, prompts, quotations and `ScreenTitle`;
  Inter (`Inter`) for everything else, including dates and headlines.
- Hairline dividers instead of shadows, no tonal elevation, generous margins (24 dp page padding),
  24 dp cards, pill buttons.
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
- `tools/verify_quotes.py` checks every quotation word for word against Rackham's text (CI runs it);
  run it after touching `quotes/Quotes.kt`.
- Play Store screenshots: `./gradlew testDebugUnitTest -Pstore=true` draws them to `app/build/store/`.
  The Play icon and feature graphic come from `tools/family_graphics.py` (see docs/family-look.md).
- Release: see `docs/play-store/RELEASE.md`. CI versionCode is 1000 + run number.
- Builds fetch from Google's Maven Central mirror first (see `settings.gradle.kts`); Robolectric uses it too.
- Commit after each milestone with a plain, descriptive message. Never put model names or identifiers in
  commits, code or docs.
- `local.properties` (SDK path) and release signing files are never committed. `app/debug.keystore` is a
  deliberate, debug-only exception so the Drive OAuth client sees a stable SHA-1 (docs/drive-setup.md).

## Website (lyceum.toolcrate.cc)
- `docs/` is the public website, served by GitHub Pages from `main` (`docs/CNAME`). It is Jekyll:
  `index.html` (home), `support.html`, `404.html`, the privacy policy and delete-your-data pages,
  all in `_layouts/default.html`; names, links and the family list live in `docs/_config.yml`.
- **Keep it true to the app.** When a change adds, removes or renames something a person would
  notice (a feature, a screen, a setting, what's stored or sent, supported Android versions,
  Play availability), update the home page's features, steps, FAQ or private panel in the same
  change, along with the README, store listing and privacy policy where they say the same thing.
  Internal refactors and fixes need no site change.
- The phone on the home page shows `docs/play-store/screenshots/` directly; when you redraw the
  store screenshots, check each slide's alt text and caption (`data-name`, `data-desc`) still fits,
  and add or drop a slide (and its dot) if the set changes.
- Follow the family look and voice (docs/family-look.md; no streaks or guilt copy). Quote only built-in copy or
  verified passages; no claims the app doesn't back up.
- Aristotle on the site comes only from `quotes/Quotes.kt`, word for word (those are verified).
- `docs/assets/site.css`, `site.js` and `_layouts/default.html` are the same in Lyceum, Telos and
  Kairos; change them in all three. Preview: `cd docs && jekyll build` (or `jekyll serve`), then
  look at the home page in light, dark and at phone width.
