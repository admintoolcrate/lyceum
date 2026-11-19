---
title: Privacy policy
permalink: /privacy/
---

# Privacy policy: Lyceum

_Effective 19 November 2026_

Lyceum is a daily reflection journal. This policy explains what happens to what you write in it.
In short: **your pages stay on your phone. If you choose a backup to your own Google Drive, it is
encrypted with a passphrase only you know, and we never receive it.**

## What the app stores

Your pages (what you write, the prompt, an optional mood word, an optional "one thing I learned"
line, tags, and an optional reference to a goal in the Stagira app), your prompts and your settings
are stored on your device, in storage only this app can read. They are excluded from Android's own
cloud backup and device-to-device transfer.

There is no Lyceum account and no Lyceum server. The app contains no analytics, no crash
reporting, no advertising and no tracking code, and nothing to buy.

## Optional backup to Google Drive

If you turn on the backup in Settings, the app saves one file to a hidden app-data folder in **your
own** Google Drive, using Google's `drive.appdata` permission. That permission lets the app see only
its own folder, not any of your other files.

Before upload, the file is encrypted on your phone (AES-256-GCM, with a key derived from your
passphrase using PBKDF2). It is then sent over an encrypted (HTTPS) connection. Without your
passphrase it can't be read by anyone, including Google and us.

- **Manual backups:** you type the passphrase each time and it is never stored.
- **Weekly backups:** to back up unattended, a key derived from your passphrase (not the passphrase
  itself) is kept on your phone, protected by Android's hardware-backed keystore.

You can delete the backup at any time with *Settings → Backup → Delete the backup from Drive*, or by
removing Lyceum's access in your Google Account (*Data & privacy → Third-party apps & services*).
Google's handling of your Drive is covered by
[Google's privacy policy](https://policies.google.com/privacy).

## Export

*Export to a file* writes your pages as a plain JSON file wherever you choose. That file is **not**
encrypted. Keep it somewhere safe.

## Stagira

If the Stagira app is installed, you can link a page to one of its goals. The two apps exchange
only a goal's id and name, directly on your phone. Nothing is sent anywhere.

## Notifications and app lock

The optional evening reminder says only "A quiet moment for today's page, whenever you like."
It never shows anything you have written. If you set an app-lock PIN, only a salted hash of it is
stored. Fingerprint or face unlock is handled by Android; the app never sees your biometric data.

## Permissions

| Permission | Why |
|---|---|
| Internet, network state | Only for the optional Google Drive backup |
| Notifications | Only for the optional evening reminder |
| Run at startup | To book the reminder again after the phone restarts |
| Biometric | Only for the optional fingerprint or face unlock |
| Wake lock | Used by Android's job scheduler to finish a reminder or backup |

## Deleting your data

Delete any page in the app, or uninstall Lyceum to remove everything stored on your phone. The Drive
backup, if you made one, can be deleted as described above.

## Children

Lyceum is not directed at children.

## Changes

If this policy changes, the new version will be posted here with a new effective date.

## Contact

Questions: **admin.toolcrate@gmail.com**
