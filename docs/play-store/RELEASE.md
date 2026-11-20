# Getting Lyceum onto Google Play

A checklist, in order. Steps marked **(you)** need your accounts or decisions; everything else is
already set up in this repo. It follows Stagira's `docs/play-store/RELEASE.md`, which was written
against Google's rules as of October 2026; check anything that may have changed since.

What's ready in the repo:

- Release signing from `keystore.properties` or CI secrets (`app/build.gradle.kts`).
- `.github/workflows/build.yml`: tests and a debug APK on every push. A release run builds the signed
  bundle and, once set up, uploads it to **Internal testing**.
- Version codes come from the CI run number (1000 + run), so every upload is higher than the last.
- Target SDK 36, which Play requires for new apps from 31 August 2026.
- Privacy policy, data-safety answers, store text, icon, feature graphic and screenshots in this folder.

---

## 1. Developer account (you)

Use the ToolCrate account Stagira is published from. Keep trader status **non-trader** and don't
add a merchant account.

## 2. Upload key (you, once; keep it safe)

On your own computer (not in the repo):

```sh
keytool -genkeypair -v -keystore lyceum-upload.jks -alias upload -keyalg RSA -keysize 4096 -validity 10000
```

Keep the `.jks` file and its passwords in a password manager. **Never commit them**: `.gitignore`
already blocks `*.jks` and `keystore.properties`. A separate key from Stagira's is tidier, but
reusing Stagira's upload key also works.

Then either:

- **Build locally:** copy `keystore.properties.example` to `keystore.properties`, fill it in, and run
  `./gradlew bundleRelease`. The bundle is `app/build/outputs/bundle/release/app-release.aab`.
- **Or let GitHub build it.** In the lyceum repo, open **Settings → Secrets and variables → Actions** and add:
  - `UPLOAD_KEYSTORE_BASE64`: the output of `base64 -w0 lyceum-upload.jks`
  - `UPLOAD_STORE_PASSWORD`, `UPLOAD_KEY_ALIAS` (`upload`), `UPLOAD_KEY_PASSWORD`

  Then run **Actions → Build → Run workflow** with **release** ticked (or push a tag such as `v1.0`).
  The run's `lyceum-play-bundle` artifact is the signed `.aab`.

## 3. Create the app in Play Console (you)

1. **Create app**: name *Lyceum*, language English, **App**, **Free**.
2. The package is `cc.toolcrate.lyceum`. It can never change once used.
3. **Upload the first bundle by hand** (Test and release → Testing → Internal testing → Create
   release) and accept **Play App Signing**. Play's upload API only works for an app that already
   has a release, so automatic uploads (step 10) start from the second build.

## 4. App content (Policy → App content) (you, about 20 minutes)

| Section | Answer |
|---|---|
| Privacy policy | The public URL from step 6 |
| Ads | **No** |
| App access | All functionality is available without special access |
| Content rating | Category **All other app types**. No violence, no user-to-user content or sharing, no purchases, no location sharing |
| Target audience | **18 and over** (as for Stagira; it keeps the app out of the Families programme, whose rules the Google sign-in for Drive backup would complicate). It can be widened later |
| Data safety | See [`data-safety.md`](data-safety.md) |
| Health apps | **My app does not have any health features.** (For Stagira, ticking a health category got it treated as a health app and rejected.) |
| Advertising ID | **No** |
| Government, financial, news | No |

Internal testing can start before every section is complete. Closed testing and production need all
of them.

## 5. Store listing (you)

Copy from [`store-listing.md`](store-listing.md). Graphics are in `graphics/` and `screenshots/`.

## 6. Host the privacy policy (you)

The repo's `docs/` folder is a small GitHub Pages site:

1. On GitHub, open **Settings → Pages**, set Source to **Deploy from a branch**, choose branch `main`
   and folder **/docs**, then **Save**. (Merge this branch into `main` first.)
2. The policy appears at `https://admintoolcrate.github.io/lyceum/privacy/`. GitHub Pages on a
   private repository needs a paid GitHub plan; otherwise make the repo public, or paste the policy
   into a Google Sites page.
3. Optional: to use `lyceum.toolcrate.cc` as Stagira does, add a `docs/CNAME` file with that name
   and a DNS `CNAME` record pointing to `admintoolcrate.github.io`.

## 7. Google sign-in for Drive backup (you)

Drive backup only connects once Google knows this app. The steps are in `docs/drive-setup.md`; for
the Play build, also:

1. In Play Console, open **Test and release → Setup → App signing** and copy the **SHA-1** of the
   *App signing key* and of the *Upload key*.
2. In Google Cloud Console → **Credentials**, add an **Android OAuth client** for each SHA-1 with package
   `cc.toolcrate.lyceum`. Keep the debug one too. The Stagira project can host these, or create a new one.
3. **OAuth consent screen**: until it is published, only test users you add can connect. That's fine
   for internal testing: add the testers' Google accounts as test users. `drive.appdata` is a
   non-sensitive scope, so publishing later needs only basic verification.

Without this, everything except Drive backup works, and the backup screen says it isn't set up.

## 8. Internal testing (you)

Internal testing has no review and reaches up to 100 testers within minutes.

1. Testing → Internal testing → **Testers**: create an email list (yourself and a few others).
2. Share the opt-in link; testers install from Play.
3. Things to check on real phones (they can't be checked in CI):
   - [ ] Write, save, see the quote; edit a past day; delete and undo.
   - [ ] Leave a page half-written, switch apps for a while, come back: the draft is still there.
   - [ ] Evening reminder: set it a few minutes ahead and see it arrive; write a page, and the next day's
         stays quiet once written; restart the phone and it still comes.
   - [ ] App lock: PIN, wrong-PIN wait, fingerprint/face, relock after 30 s away; no preview in recent apps.
   - [ ] Export to a file, uninstall, reinstall, import: everything is back.
   - [ ] Drive backup (after step 7): Manual backup and restore; wrong passphrase refused; Weekly turns on.
   - [ ] Dark mode, a large font size, a small phone.
   - [ ] Midnight: leave the app open across midnight, reopen it, and Today is the new day.

## 9. Closed testing, then production (you)

If the ToolCrate account is a **personal** account created after 13 November 2023, each new app
needs a **closed test with at least 12 testers opted in for 14 days in a row** before you can apply
for production access. Organisation accounts are exempt. After that: create a production release, roll
it out (a staged rollout such as 20% is a good start), and wait for review.

## 10. Uploads from GitHub (one-time setup)

Release runs can upload straight to **Internal testing**. Stagira's setup can be reused:

1. Play Console → **Users and permissions**: give Stagira's existing service account
   (`…@…iam.gserviceaccount.com`) access to **Lyceum** with **Release to testing tracks** and
   *View app information*. (Or create one as Stagira's RELEASE.md describes.)
2. Lyceum repo → **Settings → Secrets and variables → Actions**: add `PLAY_SERVICE_ACCOUNT_JSON`,
   the service account's JSON key.

From then on, **Actions → Build → Run workflow → release** (or a `v*` tag) sends a build to Internal
testing, with the commit's first line as the testers' "What's new". Promoting to closed testing or
production stays a click in Play Console.

## Every update

- CI numbers builds itself. For a local build, raise `versionCode` in `app/build.gradle.kts`
  above the last one Play has.
- Raise `versionName` for releases people will see (it's 1.0 now).
