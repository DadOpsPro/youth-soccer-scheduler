package com.dadops.soccer.conflict;

import com.dadops.soccer.domain.Event;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Shared scan used by rules that ask "did one resource get booked twice at once?".
 * The key is whatever the rule treats as the shared resource, such as a coach id
 * or a venue id.
 */
final class OverlappingPairs {

    private static final Comparator<EventPair> BY_TIME_THEN_ID = Comparator
            .comparing((EventPair pair) -> pair.earlier().start())
            .thenComparing(pair -> pair.earlier().id())
            .thenComparing(pair -> pair.later().id());

    private OverlappingPairs() {
    }

    static List<EventPair> groupedBy(List<Event> events, Function<Event, String> key) {
        Map<String, List<Event>> groups = events.stream()
                .collect(Collectors.groupingBy(key, TreeMap::new, Collectors.toList()));

        List<EventPair> pairs = new ArrayList<>();
        for (List<Event> group : groups.values()) {
            List<Event> sorted = group.stream()
                    .sorted(Comparator.comparing(Event::start).thenComparing(Event::id))
                    .toList();
            for (int i = 0; i < sorted.size(); i++) {
                for (int j = i + 1; j < sorted.size(); j++) {
                    if (TimeWindows.overlap(sorted.get(i), sorted.get(j))) {
                        pairs.add(new EventPair(sorted.get(i), sorted.get(j)));
                    }
                }
            }
        }
        return pairs.stream().sorted(BY_TIME_THEN_ID).toList();
    }
}
