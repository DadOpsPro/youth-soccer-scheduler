package com.dadops.soccer.conflict;

import static com.dadops.soccer.TestData.BLUE;
import static com.dadops.soccer.TestData.FIELD_2;
import static com.dadops.soccer.TestData.FIELD_5;
import static com.dadops.soccer.TestData.OKAFOR;
import static com.dadops.soccer.TestData.RED;
import static com.dadops.soccer.TestData.RIVERA;
import static com.dadops.soccer.TestData.event;
import static com.dadops.soccer.domain.EventType.GAME;
import static com.dadops.soccer.domain.EventType.PRACTICE;
import static org.assertj.core.api.Assertions.assertThat;

import com.dadops.soccer.domain.Event;
import java.util.List;
import org.junit.jupiter.api.Test;

class VenueDoubleBookingRuleTest {

    private final VenueDoubleBookingRule rule = new VenueDoubleBookingRule();

    @Test
    void findsNothingWhenTheSameFieldIsUsedAtDifferentTimes() {
        List<Event> events = List.of(
                event("E1", PRACTICE, BLUE, RIVERA, FIELD_2, "2026-03-03T17:00", "2026-03-03T18:00"),
                event("E2", GAME, RED, OKAFOR, FIELD_2, "2026-03-03T19:00", "2026-03-03T20:00"));

        assertThat(rule.detect(events)).isEmpty();
    }

    @Test
    void findsNothingWhenOverlappingEventsAreAtDifferentFields() {
        List<Event> events = List.of(
                event("E1", PRACTICE, BLUE, RIVERA, FIELD_2, "2026-03-03T18:00", "2026-03-03T19:00"),
                event("E2", GAME, RED, OKAFOR, FIELD_5, "2026-03-03T18:00", "2026-03-03T19:00"));

        assertThat(rule.detect(events)).isEmpty();
    }

    @Test
    void findsAnExactOverlap() {
        List<Event> events = List.of(
                event("E1", PRACTICE, BLUE, RIVERA, FIELD_2, "2026-03-03T18:00", "2026-03-03T19:00"),
                event("E2", GAME, RED, OKAFOR, FIELD_2, "2026-03-03T18:00", "2026-03-03T19:00"));

        List<Conflict> conflicts = rule.detect(events);

        assertThat(conflicts).hasSize(1);
        assertThat(conflicts.getFirst().type()).isEqualTo(ConflictType.VENUE_DOUBLE_BOOKED);
        assertThat(conflicts.getFirst().events()).extracting(Event::id).containsExactly("E1", "E2");
    }

    @Test
    void findsAPartialOverlapAndExplainsTheSharedWindow() {
        List<Event> events = List.of(
                event("E1", PRACTICE, BLUE, RIVERA, FIELD_2, "2026-03-03T18:00", "2026-03-03T19:00"),
                event("E2", GAME, RED, OKAFOR, FIELD_2, "2026-03-03T18:30", "2026-03-03T20:00"));

        List<Conflict> conflicts = rule.detect(events);

        assertThat(conflicts).hasSize(1);
        assertThat(conflicts.getFirst().explanation()).isEqualTo(
                "Field 2 is double-booked: U10 Blue practice (Coach Rivera) and U12 Red game "
                        + "(Coach Okafor) from 6:30 PM to 7:00 PM on Tue Mar 3.");
    }

    @Test
    void findsNothingWhenEventsTouchButDoNotOverlap() {
        List<Event> events = List.of(
                event("E1", PRACTICE, BLUE, RIVERA, FIELD_2, "2026-03-03T18:00", "2026-03-03T19:00"),
                event("E2", GAME, RED, OKAFOR, FIELD_2, "2026-03-03T19:00", "2026-03-03T20:00"));

        assertThat(rule.detect(events)).isEmpty();
    }
}
