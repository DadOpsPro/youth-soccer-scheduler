# Conflicts span Seasons; Schedules are per-Season views

Seasons overlap: a winter indoor league can still be running when spring tryouts begin, and the same Coaches and Fields work in both. If Conflicts were only checked inside one Season's Schedule, the hardest double-bookings would go unreported. So Coaches, Venues and Fields are shared across all Seasons, and Conflict checks run over every loaded Event whose time overlaps another, whichever Season it belongs to. A Schedule stays one Season's view, used for loading, viewing and exporting. It is not the boundary for Conflicts.

## Consequences

- **Storage.** For now, one in-memory store holds every loaded Season. An import adds one Season, or replaces it if it is already loaded. It never replaces everything that has been loaded. The Coordinator names the Season on the upload form.
- **Teams are per-Season.** Unlike Coaches and Fields, a Team belongs to one Season, because rosters change every Season.
- **Identity.** A Coach or Field is recognised across Seasons by a stable id that the Coordinator owns. If an id arrives with a different name from the one already loaded, the import reports it rather than guessing which name is right. A name alone never decides identity, because two different coaches can share a name.
- **Outside systems.** Club exports from TeamSnap, GotSport or a league system carry their own ids. Mapping those ids to the Coordinator's ids is a separate import step for later. Until it exists, the Coordinator prepares the CSVs and keeps the ids stable.

## Considered Options

- **Conflicts only inside a Schedule.** Simpler, and each Season would stand alone. Rejected because a Coach or Field shared between overlapping Seasons would never be checked, and the Coordinator would have no warning that the gap exists.
