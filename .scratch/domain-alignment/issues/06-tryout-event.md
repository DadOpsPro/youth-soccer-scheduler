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
- The README's CSV section documents how a Tryout row is written.

## Open questions

- In `events.csv`, does a Tryout row put the Age Group in `team_id`, or does it get its own column?
