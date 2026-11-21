# Rename Venue to Field

Status: needs-triage

## What

In the glossary, a **Venue** is a location (Scheels Soccer Complex) and a **Field** is a bookable surface at it (Field 1). What the code calls `Venue` today (`domain/Venue.java`, `venues.csv`, `VenueDoubleBookingRule`, `VENUE_DOUBLE_BOOKED`) is actually a Field. Rename it to Field throughout. The parent Venue waits for the travel-time feature.

## Why

So the code uses the same words as `GLOSSARY.md`. The UI already says "Field double-booked".

## Done when

- The domain, the rule, the conflict type and the CSV use Field for the bookable surface.
- `venues.csv` becomes `fields.csv` (`field_id,field_name`), and the README is updated to match.
- Field double-booking still compares Fields.

## Decided

- **The parent Venue waits** for the travel-time roadmap item, which is the first thing that needs it.
