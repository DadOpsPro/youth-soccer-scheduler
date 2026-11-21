# Add Tryout as a third kind of Event

Status: needs-triage

## What

Add **Tryout** alongside Game and Practice. Keep it minimal: an Age Group, a Coach, a Field and a window of time. A Tryout has no Team and no Opponent. Coaching clinics, referee courses and similar Events are out of scope.

## Why

Spring tryouts can overlap a winter indoor league, which is exactly the cross-Season case in ADR 0001. Today a tryout can only be entered as a Practice under some Team, so the conflict sentence reads "U12 Red practice" for something that isn't one. See **Tryout** and **Age Group** in `GLOSSARY.md`.

## Done when

- An Event can be a Tryout for an Age Group, with no Team.
- Tryouts take part in Coach and Field double-booking like any other Event.
- A conflict sentence describes one as, for example, "U12 tryout".
- `events.csv` gains an `age_group` column. A Tryout row fills `age_group` and leaves `team_id` empty. A Game or Practice row fills `team_id` and leaves `age_group` empty. A row that fills both, or neither, is rejected with its line number.
- The README's CSV section documents the new column and how a Tryout row is written.

## Decided

- **Tryouts get their own `age_group` column.** Reusing `team_id` for something that isn't a Team is exactly the kind of confusing shortcut the glossary exists to prevent.

## Note

The importer requires headers to match exactly, so every existing `events.csv`, including `sample-data/events.csv`, needs the new column added. Any `events.csv` without it will be refused.
