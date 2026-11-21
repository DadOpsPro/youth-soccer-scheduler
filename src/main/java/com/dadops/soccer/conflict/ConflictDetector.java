package com.dadops.soccer.conflict;

import com.dadops.soccer.domain.Event;
import java.util.Comparator;
import java.util.List;

/** Runs every rule over the schedule and returns the findings in time order. */
public final class ConflictDetector {

    private static final Comparator<Conflict> BY_FIRST_EVENT = Comparator
            .comparing((Conflict conflict) -> conflict.events().getFirst().start())
            .thenComparing(conflict -> conflict.type().name())
            .thenComparing(Conflict::explanation);

    private final List<ConflictRule> rules;

    public ConflictDetector(List<ConflictRule> rules) {
        this.rules = List.copyOf(rules);
    }

    /** The rules this detector was built with, in order. */
    public List<ConflictRule> rules() {
        return rules;
    }

    public List<Conflict> detect(List<Event> events) {
        return rules.stream()
                .flatMap(rule -> rule.detect(events).stream())
                .sorted(BY_FIRST_EVENT)
                .toList();
    }
}
