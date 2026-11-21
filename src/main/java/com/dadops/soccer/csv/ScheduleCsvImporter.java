package com.dadops.soccer.csv;

import com.dadops.soccer.domain.Coach;
import com.dadops.soccer.domain.Event;
import com.dadops.soccer.domain.EventType;
import com.dadops.soccer.domain.Team;
import com.dadops.soccer.domain.Venue;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.csv.CSVRecord;

/** Turns four uploaded CSV files into a schedule, collecting problems as it goes. */
public final class ScheduleCsvImporter {

    public static final List<String> TEAM_HEADERS = List.of("team_id", "team_name");
    public static final List<String> COACH_HEADERS = List.of("coach_id", "coach_name");
    public static final List<String> VENUE_HEADERS = List.of("venue_id", "venue_name");
    public static final List<String> EVENT_HEADERS =
            List.of("event_id", "event_type", "team_id", "coach_id", "venue_id", "start", "end");

    /** The one timestamp format accepted in CSV, for example 2026-03-03T18:00. */
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final int MAX_ECHOED_VALUE_LENGTH = 40;

    private final ZoneId zone;
    private final CsvLimits limits;

    public ScheduleCsvImporter(ZoneId zone, CsvLimits limits) {
        this.zone = zone;
        this.limits = limits;
    }

    public ImportResult importAll(CsvUpload teams, CsvUpload coaches, CsvUpload venues, CsvUpload events) {
        List<ImportProblem> problems = new ArrayList<>();

        Map<String, Team> teamsById =
                readLookup(teams, TEAM_HEADERS, "team_id", "team_name", Team::new, problems);
        Map<String, Coach> coachesById =
                readLookup(coaches, COACH_HEADERS, "coach_id", "coach_name", Coach::new, problems);
        Map<String, Venue> venuesById =
                readLookup(venues, VENUE_HEADERS, "venue_id", "venue_name", Venue::new, problems);
        List<Event> parsedEvents = readEvents(events, teamsById, coachesById, venuesById, problems);

        return new ImportResult(
                List.copyOf(teamsById.values()),
                List.copyOf(coachesById.values()),
                List.copyOf(venuesById.values()),
                parsedEvents,
                problems);
    }

    /** Reads one of the three id/name files. All three have the same shape. */
    private <T> Map<String, T> readLookup(
            CsvUpload upload,
            List<String> headers,
            String idColumn,
            String nameColumn,
            NamedFactory<T> factory,
            List<ImportProblem> problems) {

        Map<String, T> byId = new LinkedHashMap<>();
        CsvTable table = readTable(upload, headers, problems);
        if (table == null) {
            return byId;
        }
        for (CSVRecord row : table.rows()) {
            try {
                String id = required(row, idColumn);
                String name = required(row, nameColumn);
                if (byId.putIfAbsent(id, factory.create(id, name)) != null) {
                    throw new RowException("duplicate %s %s".formatted(idColumn, quoted(id)));
                }
            } catch (RowException e) {
                problems.add(ImportProblem.ofLine(table.fileName(), lineOf(row), e.getMessage()));
            }
        }
        return byId;
    }

    private List<Event> readEvents(
            CsvUpload upload,
            Map<String, Team> teams,
            Map<String, Coach> coaches,
            Map<String, Venue> venues,
            List<ImportProblem> problems) {

        List<Event> events = new ArrayList<>();
        CsvTable table = readTable(upload, EVENT_HEADERS, problems);
        if (table == null) {
            return events;
        }
        Map<String, Event> byId = new LinkedHashMap<>();
        for (CSVRecord row : table.rows()) {
            try {
                String id = required(row, "event_id");
                if (byId.containsKey(id)) {
                    throw new RowException("duplicate event_id " + quoted(id));
                }
                Event event = new Event(
                        id,
                        parseEventType(row),
                        lookUp(row, "team_id", teams, "team"),
                        lookUp(row, "coach_id", coaches, "coach"),
                        lookUp(row, "venue_id", venues, "field"),
                        parseTimestamp(row, "start"),
                        parseTimestamp(row, "end"));
                byId.put(id, event);
                events.add(event);
            } catch (RowException e) {
                problems.add(ImportProblem.ofLine(table.fileName(), lineOf(row), e.getMessage()));
            } catch (IllegalArgumentException e) {
                // Raised by Event itself, for instance when end is not after start.
                problems.add(ImportProblem.ofLine(table.fileName(), lineOf(row), e.getMessage()));
            }
        }
        return events;
    }

    /** Returns null and records a problem when the file cannot be used at all. */
    private CsvTable readTable(CsvUpload upload, List<String> headers, List<ImportProblem> problems) {
        try {
            return CsvTable.read(upload, headers, limits);
        } catch (CsvFormatException e) {
            problems.add(ImportProblem.ofFile(upload.fileName(), e.getMessage()));
            return null;
        }
    }

    private static String required(CSVRecord row, String column) throws RowException {
        String value = row.isMapped(column) ? row.get(column) : null;
        if (value == null || value.isBlank()) {
            throw new RowException(column + " is missing");
        }
        return value;
    }

    private static <T> T lookUp(CSVRecord row, String column, Map<String, T> byId, String label)
            throws RowException {
        String id = required(row, column);
        T found = byId.get(id);
        if (found == null) {
            throw new RowException("unknown %s %s".formatted(label, quoted(id)));
        }
        return found;
    }

    private static EventType parseEventType(CSVRecord row) throws RowException {
        String raw = required(row, "event_type");
        try {
            return EventType.valueOf(raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new RowException("event_type must be game or practice, got " + quoted(raw));
        }
    }

    private ZonedDateTime parseTimestamp(CSVRecord row, String column) throws RowException {
        String raw = required(row, column);
        try {
            return LocalDateTime.parse(raw, TIMESTAMP).atZone(zone);
        } catch (DateTimeParseException e) {
            throw new RowException("%s must look like 2026-03-03T18:00, got %s".formatted(column, quoted(raw)));
        }
    }

    /**
     * The 1-based line in the file. Commons CSV numbers data rows from 1 with the
     * header skipped, so adding one lines up with what an editor shows for rows
     * that do not contain quoted newlines.
     */
    private static long lineOf(CSVRecord row) {
        return row.getRecordNumber() + 1;
    }

    /**
     * Echoes a cell back inside quotes, shortened first. Keeping it short stops one
     * oversized cell from flooding the problem list; the page escapes the value.
     */
    private static String quoted(String value) {
        String shortened = value.length() <= MAX_ECHOED_VALUE_LENGTH
                ? value
                : value.substring(0, MAX_ECHOED_VALUE_LENGTH) + "...";
        return "\"" + shortened + "\"";
    }

    /** Builds a Team, Coach, or Venue from its id and name. */
    private interface NamedFactory<T> {
        T create(String id, String name);
    }
}
