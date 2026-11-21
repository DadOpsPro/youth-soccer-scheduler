# Rename the code's Schedule to match the glossary

Status: needs-triage
Blocked by: 01

## What

In the code, `Schedule` means everything loaded at once. In `GLOSSARY.md`, a **Schedule** is one Season's Events. Once 01 lands, rename the everything-loaded type to something else, and keep `Schedule` for one Season's view.

## Why

ADR 0001: a Schedule is a per-Season view for loading, viewing and exporting, not the boundary for Conflicts. A type called `Schedule` that holds every Season will mislead the next reader.

## Done when

- No type called `Schedule` holds more than one Season.
- Conflict detection runs over the everything-loaded collection, not over a single Schedule.
