package com.dadops.soccer.conflict;

import com.dadops.soccer.domain.Event;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Overlap arithmetic and the wording used to describe a time window. */
public final class TimeWindows {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("h:mm a", Locale.US);
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEE MMM d", Locale.US);

    private TimeWindows() {
    }

    /**
     * True when the two events share any amount of time. Events that merely touch
     * (one ends exactly when the other starts) do not overlap.
     */
    public static boolean overlap(Event a, Event b) {
        return a.start().isBefore(b.end()) && b.start().isBefore(a.end());
    }

    /** Start of the shared window. Only meaningful when the events overlap. */
    public static ZonedDateTime overlapStart(Event a, Event b) {
        return a.start().isAfter(b.start()) ? a.start() : b.start();
    }

    /** End of the shared window. Only meaningful when the events overlap. */
    public static ZonedDateTime overlapEnd(Event a, Event b) {
        return a.end().isBefore(b.end()) ? a.end() : b.end();
    }

    /** For example "from 6:00 PM to 6:30 PM on Tue Mar 3". */
    public static String describeOverlap(Event a, Event b) {
        ZonedDateTime from = overlapStart(a, b);
        ZonedDateTime to = overlapEnd(a, b);
        if (from.toLocalDate().equals(to.toLocalDate())) {
            return "from %s to %s on %s".formatted(TIME.format(from), TIME.format(to), DAY.format(from));
        }
        return "from %s on %s to %s on %s"
                .formatted(TIME.format(from), DAY.format(from), TIME.format(to), DAY.format(to));
    }
}
