package com.dadops.soccer.web;

import com.dadops.soccer.domain.Event;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * One row of the events table with its times already formatted, so the template
 * stays free of date handling.
 */
public record EventRow(String id, String type, String team, String coach, String venue, String when) {

    private static final DateTimeFormatter FULL = DateTimeFormatter.ofPattern("EEE MMM d, h:mm a", Locale.US);
    private static final DateTimeFormatter TIME_ONLY = DateTimeFormatter.ofPattern("h:mm a", Locale.US);

    public static EventRow of(Event event) {
        return new EventRow(
                event.id(),
                event.type().label(),
                event.team().name(),
                event.coach().name(),
                event.venue().name(),
                formatWindow(event));
    }

    private static String formatWindow(Event event) {
        String start = FULL.format(event.start());
        boolean sameDay = event.start().toLocalDate().equals(event.end().toLocalDate());
        String end = sameDay ? TIME_ONLY.format(event.end()) : FULL.format(event.end());
        return start + " to " + end;
    }
}
