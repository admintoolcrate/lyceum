# Lyceum ↔ Stagira

A page in Lyceum can mention a goal kept in [Stagira](https://github.com/admintoolcrate/Stagira).
The two apps talk only through Android intents on the phone: no server, no shared database, no
account. Lyceum stores the goal's **id** and a **copy of its title** with the page, and nothing more.
Lyceum works fully without Stagira: when no installed app answers these intents, the link controls
are hidden.

Constants live in `app/src/main/java/cc/toolcrate/lyceum/stagira/StagiraContract.kt`.

## Lyceum → Stagira

| Purpose | Intent | Extras | Result |
|---|---|---|---|
| Pick a goal | action `cc.toolcrate.stagira.action.PICK_GOAL`, package `cc.toolcrate.stagira`, started for a result | none | `RESULT_OK` with `goal_id` (String, required, ≤ 128 chars) and `goal_title` (String, optional, ≤ 120 chars); `RESULT_CANCELED` if the user backs out |
| Open a goal | action `cc.toolcrate.stagira.action.VIEW_GOAL`, package `cc.toolcrate.stagira` | `goal_id` | none |

Lyceum declares both actions under `<queries>` so it can tell whether Stagira handles them.

## Stagira → Lyceum

| Purpose | Intent | Extras |
|---|---|---|
| Write about a goal | action `cc.toolcrate.lyceum.action.NEW_ENTRY`, package `cc.toolcrate.lyceum` | optional `goal_id`, `goal_title` |

Lyceum opens today's page with the goal linked, **unsaved**. The user writes and saves, or leaves.
If the app lock is on, the lock screen comes first. Bad extras (blank, too long, control characters)
are ignored.

## What Stagira would add (not built yet)

- An exported activity for `PICK_GOAL` that lists goals and returns the chosen one. It should leave
  out private goals, or return only their cover names.
- An exported activity, or an intent filter on the main one, for `VIEW_GOAL` that opens the goal page.
- Optionally, a "Write about this in Lyceum" action that sends `NEW_ENTRY`.
