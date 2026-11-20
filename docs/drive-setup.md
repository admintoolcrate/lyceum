# Setting up the optional Google Drive backup

Lyceum has no server and no account of its own. The Drive backup uses Google's own authorization to
reach a hidden **app data folder** in the user's Drive (scope `drive.appdata`). Lyceum can't see
anything else in that Drive, and nothing else can see the folder.

Google only grants that access to an app it knows. Each package name and signing certificate pair
needs an Android OAuth client in a Google Cloud project. Until one exists, Settings → Backup shows
"Google sign-in isn't set up for this copy of Lyceum yet". The rest of the app is unaffected.

## Steps

1. In the [Google Cloud console](https://console.cloud.google.com/), create a project (or reuse one).
2. **APIs & Services → Library**: enable the **Google Drive API**.
3. **APIs & Services → OAuth consent screen**: set it up as *External*, add the scope
   `https://www.googleapis.com/auth/drive.appdata`, and add yourself as a test user while testing.
4. **APIs & Services → Credentials → Create credentials → OAuth client ID → Android**:
   - Package name: `cc.toolcrate.lyceum`
   - SHA-1: the signing certificate's fingerprint.
     - Debug builds are signed with the committed `app/debug.keystore`. Its SHA-1 is
       `E7:6A:CE:3F:98:CF:64:95:0A:3B:4B:4B:AE:2E:9E:A1:69:85:1F:E6`.
     - Release builds: run `keytool -list -v -keystore <your upload keystore>` and, for Play, also add
       the **app signing** key's SHA-1 from the Play Console.
5. Nothing goes into the app's code or resources: Google matches the package and certificate.

## What is uploaded

One file, `lyceum-backup.json`, replaced on each backup. It is the JSON export sealed with
AES-256-GCM under a key derived from the user's passphrase (PBKDF2-HMAC-SHA256, 210,000 iterations,
random salt and IV). See `app/src/main/java/cc/toolcrate/lyceum/backup/Envelope.kt`.

- **Manual**: the passphrase is typed for each backup or restore and is never stored.
- **Weekly**: the derived key (not the passphrase) is kept on the phone, wrapped by an Android Keystore
  key, so the weekly job can run unattended. It runs on unmetered networks with enough battery.
