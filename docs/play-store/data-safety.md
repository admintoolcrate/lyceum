# Data safety form: suggested answers

Play Console → Policy → App content → Data safety. These follow how the app actually behaves
(see `privacy-policy.md`). They are deliberately conservative: when in doubt, declare.

## Overview

| Question | Answer |
|---|---|
| Does your app collect or share any of the required user data types? | **Yes** (only when the user turns on the Google Drive backup) |
| Is all of the user data collected by your app encrypted in transit? | **Yes** (HTTPS to Google Drive, and sealed with the user's passphrase before that) |
| Do you provide a way for users to request that their data is deleted? | **Yes** (*Delete the backup from Drive* in Settings, deleting pages, or uninstalling) |

## Data types

Declare as **Collected**, **not shared**, **optional**, purpose **App functionality** only:

| Category | Type | Why |
|---|---|---|
| App activity | Other user-generated content | Journal pages inside the encrypted Drive backup |

The backup is end-to-end encrypted and goes to the user's own Drive, so the developer never receives
it. Declaring it anyway is the cautious choice, and it matches how Stagira was declared.

**Not** health info: moods are words the user picks, not health data, and the app makes no health
claims. Declaring health info would also clash with the Health apps declaration ("no health
features").

Everything else (location, personal info, contacts, financial info, messages, photos, audio, files,
calendar, web history, app interactions, diagnostics, device or other IDs): **not collected**. No
ads, no purchases, no advertising ID.

## Security section

- **Data is encrypted in transit:** Yes.
- **You can request that data be deleted:** Yes.
