package com.dadops.soccer.domain;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A game or practice: one team, one coach, one venue, and a time window.
 *
 * <p>Times are zoned so that comparisons stay correct across daylight-saving
 * changes. A game involving two teams is currently modelled from the home
 * team's point of view (see the README roadmap).
 */
public record Event(
        String id,
        EventType type,
        Team team,
        Coach coach,
        Venue venue,
        ZonedDateTime start,
        ZonedDateTime end) {

    public Event {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(team, "team");
        Objects.requireNonNull(coach, "coach");
        Objects.requireNonNull(venue, "venue");
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        if (!end.toInstant().isAfter(start.toInstant())) {
            throw new IllegalArgumentException("end must be after start for event " + id);
        }
    }

    public Duration duration() {
        return Duration.between(start, end);
    }

    /** "U10 Blue practice" — the phrase used when describing this event. */
    public String describe() {
        return team.name() + " " + type.label();
    }
}
