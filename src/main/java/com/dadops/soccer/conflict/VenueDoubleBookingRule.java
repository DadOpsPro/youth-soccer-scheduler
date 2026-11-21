package com.dadops.soccer.conflict;

import com.dadops.soccer.domain.Event;
import java.util.List;

/** Flags a field booked by two overlapping events. */
public final class VenueDoubleBookingRule implements ConflictRule {

    @Override
    public String name() {
        return "Field double-booking";
    }

    @Override
    public List<Conflict> detect(List<Event> events) {
        return OverlappingPairs.groupedBy(events, event -> event.venue().id()).stream()
                .map(pair -> new Conflict(
                        ConflictType.VENUE_DOUBLE_BOOKED,
                        explain(pair.earlier(), pair.later()),
                        List.of(pair.earlier(), pair.later())))
                .toList();
    }

    private String explain(Event earlier, Event later) {
        return "%s is double-booked: %s (%s) and %s (%s) %s."
                .formatted(
                        earlier.venue().name(),
                        earlier.describe(),
                        earlier.coach().name(),
                        later.describe(),
                        later.coach().name(),
                        TimeWindows.describeOverlap(earlier, later));
    }
}
