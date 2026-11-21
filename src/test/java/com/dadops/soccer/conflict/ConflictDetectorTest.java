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

class ConflictDetectorTest {

    private final ConflictDetector detector =
            new ConflictDetector(List.of(new CoachDoubleBookingRule(), new VenueDoubleBookingRule()));

    @Test
    void returnsNothingForAnEmptySchedule() {
        assertThat(detector.detect(List.of())).isEmpty();
    }

    @Test
    void collectsFindingsFromEveryRuleInTimeOrder() {
        List<Event> events = List.of(
                // Thursday: one field used by two coaches at once.
                event("E5", GAME, BLUE, RIVERA, FIELD_2, "2026-03-05T18:00", "2026-03-05T19:30"),
                event("E6", GAME, RED, OKAFOR, FIELD_2, "2026-03-05T19:00", "2026-03-05T20:30"),
                // Tuesday: one coach expected at two fields at once.
                event("E1", PRACTICE, BLUE, RIVERA, FIELD_2, "2026-03-03T18:00", "2026-03-03T19:00"),
                event("E2", GAME, RED, RIVERA, FIELD_5, "2026-03-03T18:30", "2026-03-03T20:00"));

        List<Conflict> conflicts = detector.detect(events);

        assertThat(conflicts).extracting(Conflict::type).containsExactly(
                ConflictType.COACH_DOUBLE_BOOKED, ConflictType.VENUE_DOUBLE_BOOKED);
    }

    @Test
    void reportsBothRulesWhenOnePairBreaksBoth() {
        List<Event> events = List.of(
                event("E1", PRACTICE, BLUE, RIVERA, FIELD_2, "2026-03-03T18:00", "2026-03-03T19:00"),
                event("E2", GAME, RED, RIVERA, FIELD_2, "2026-03-03T18:30", "2026-03-03T20:00"));

        assertThat(detector.detect(events)).extracting(Conflict::type).containsExactly(
                ConflictType.COACH_DOUBLE_BOOKED, ConflictType.VENUE_DOUBLE_BOOKED);
    }
}
