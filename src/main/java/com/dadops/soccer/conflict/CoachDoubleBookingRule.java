package com.dadops.soccer.conflict;

import com.dadops.soccer.domain.Event;
import java.util.List;

/** Flags a coach who is expected at two overlapping events. */
public final class CoachDoubleBookingRule implements ConflictRule {

    @Override
    public String name() {
        return "Coach double-booking";
    }

    @Override
    public List<Conflict> detect(List<Event> events) {
        return OverlappingPairs.groupedBy(events, event -> event.coach().id()).stream()
                .map(pair -> new Conflict(
                        ConflictType.COACH_DOUBLE_BOOKED,
                        explain(pair.earlier(), pair.later()),
                        List.of(pair.earlier(), pair.later())))
                .toList();
    }

    private String explain(Event earlier, Event later) {
        return "%s is booked at %s (%s) and %s (%s) %s."
                .formatted(
                        earlier.coach().name(),
                        earlier.venue().name(),
                        earlier.describe(),
                        later.venue().name(),
                        later.describe(),
                        TimeWindows.describeOverlap(earlier, later));
    }
}
