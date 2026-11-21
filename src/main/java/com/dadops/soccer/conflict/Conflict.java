package com.dadops.soccer.conflict;

import com.dadops.soccer.domain.Event;
import java.util.List;
import java.util.Objects;

/**
 * One detected scheduling problem, already written out in plain language.
 *
 * @param explanation a full sentence a coach or manager can act on
 * @param events      the events involved, earliest first
 */
public record Conflict(ConflictType type, String explanation, List<Event> events) {

    public Conflict {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(explanation, "explanation");
        events = List.copyOf(events);
        if (events.isEmpty()) {
            throw new IllegalArgumentException("a conflict must reference at least one event");
        }
    }
}
