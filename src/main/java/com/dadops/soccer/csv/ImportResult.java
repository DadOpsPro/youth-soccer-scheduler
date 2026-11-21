package com.dadops.soccer.csv;

import com.dadops.soccer.domain.Coach;
import com.dadops.soccer.domain.Event;
import com.dadops.soccer.domain.Team;
import com.dadops.soccer.domain.Venue;
import java.util.List;

/**
 * What an import produced: everything that parsed cleanly, plus a problem entry
 * for every file or row that did not. A bad row never stops the import.
 */
public record ImportResult(
        List<Team> teams,
        List<Coach> coaches,
        List<Venue> venues,
        List<Event> events,
        List<ImportProblem> problems) {

    public ImportResult {
        teams = List.copyOf(teams);
        coaches = List.copyOf(coaches);
        venues = List.copyOf(venues);
        events = List.copyOf(events);
        problems = List.copyOf(problems);
    }

    public boolean hasProblems() {
        return !problems.isEmpty();
    }
}
