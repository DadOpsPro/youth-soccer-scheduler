package com.dadops.soccer;

import com.dadops.soccer.domain.Coach;
import com.dadops.soccer.domain.Event;
import com.dadops.soccer.domain.EventType;
import com.dadops.soccer.domain.Team;
import com.dadops.soccer.domain.Venue;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Synthetic fixtures shared by the tests. Every name here is invented; no real
 * club, team, coach, or family appears anywhere in this repository.
 */
public final class TestData {

    public static final ZoneId ZONE = ZoneId.of("America/New_York");

    public static final Team BLUE = new Team("T1", "U10 Blue");
    public static final Team WHITE = new Team("T2", "U10 White");
    public static final Team RED = new Team("T3", "U12 Red");

    public static final Coach RIVERA = new Coach("C1", "Coach Rivera");
    public static final Coach OKAFOR = new Coach("C2", "Coach Okafor");

    public static final Venue FIELD_2 = new Venue("V2", "Field 2");
    public static final Venue FIELD_5 = new Venue("V3", "Field 5");

    private TestData() {
    }

    /** Builds an event from ISO local times, for example "2026-03-03T18:00". */
    public static Event event(
            String id, EventType type, Team team, Coach coach, Venue venue, String start, String end) {
        return new Event(
                id,
                type,
                team,
                coach,
                venue,
                LocalDateTime.parse(start).atZone(ZONE),
                LocalDateTime.parse(end).atZone(ZONE));
    }
}
