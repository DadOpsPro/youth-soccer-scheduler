package com.dadops.soccer.domain;

import java.util.List;

/** Everything currently loaded: the lookup lists plus the events built from them. */
public record Schedule(List<Team> teams, List<Coach> coaches, List<Venue> venues, List<Event> events) {

    public Schedule {
        teams = List.copyOf(teams);
        coaches = List.copyOf(coaches);
        venues = List.copyOf(venues);
        events = List.copyOf(events);
    }

    public static Schedule empty() {
        return new Schedule(List.of(), List.of(), List.of(), List.of());
    }

    public boolean isEmpty() {
        return events.isEmpty() && teams.isEmpty() && coaches.isEmpty() && venues.isEmpty();
    }
}
