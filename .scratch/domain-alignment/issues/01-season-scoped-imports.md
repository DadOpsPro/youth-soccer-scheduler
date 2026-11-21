# Season-scoped imports: add or replace one Season

Status: needs-triage

## What

An import should add one Season to the store, or replace it if that Season is already loaded. Right now `ScheduleRepository.replaceAll` (and the controller's call to it) throws away everything already loaded. Conflict checks then run across every loaded Event, whichever Season it belongs to.

## Why

This is the core of ADR 0001 (`docs/adr/0001-conflicts-span-seasons.md`). As things stand, loading spring tryouts wipes the winter indoor league, so a Coach or Field booked in both Seasons at the same time is never reported.

## Done when

- Loading Season A and then Season B keeps both loaded, and a Coach double-booking between an A Event and a B Event is reported.
- Loading Season A a second time replaces A's Events without touching B.
- The page makes clear which Seasons are loaded.
- The Coordinator names the Season on the upload form. The CSV formats don't change.
- Teams belong to the Season they were loaded with, because rosters change every Season. Coaches and Fields are shared across Seasons.

## Decided

- **Season comes from the upload form**, not a CSV column, because that's simpler.
- **Teams are per-Season.** Rosters change each Season, but coaches and fields don't.
