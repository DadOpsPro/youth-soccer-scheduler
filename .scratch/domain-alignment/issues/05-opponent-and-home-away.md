# Give Games an Opponent and Home / Away

Status: needs-triage

## What

A Game should record its Opponent and whether the Team is Home or Away. Today `Event` holds one Team only. This replaces the README roadmap item "Opponents on games", and should fold in Home / Away.

## Why

Home / Away decides which Kit a Team wears, and it is set for each Game rather than worked out from the Venue (see `GLOSSARY.md`). An Opponent can be another Team being coordinated, so a club derby is one Game, not two Events that look like a Field double-booking.

## Done when

- A Game has an Opponent and a Home / Away value. Practices have neither.
- A derby between two coordinated Teams is one Game and produces no Field double-booking.
- The `Event.java` comment about "the home team's point of view" is gone.
