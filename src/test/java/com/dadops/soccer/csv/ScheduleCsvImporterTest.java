package com.dadops.soccer.csv;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import com.dadops.soccer.TestData;
import com.dadops.soccer.domain.Event;
import com.dadops.soccer.domain.EventType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ScheduleCsvImporterTest {

    private static final String TEAMS = """
            team_id,team_name
            T1,U10 Blue
            T3,U12 Red
            """;
    private static final String COACHES = """
            coach_id,coach_name
            C1,Coach Rivera
            C2,Coach Okafor
            """;
    private static final String VENUES = """
            venue_id,venue_name
            V2,Field 2
            V3,Field 5
            """;
    private static final String EVENTS = """
            event_id,event_type,team_id,coach_id,venue_id,start,end
            E1,practice,T1,C1,V2,2026-03-03T18:00,2026-03-03T19:00
            E2,game,T3,C1,V3,2026-03-03T18:30,2026-03-03T20:00
            """;

    private final ScheduleCsvImporter importer =
            new ScheduleCsvImporter(TestData.ZONE, CsvLimits.DEFAULT);

    @Test
    void importsWellFormedFiles() {
        ImportResult result = importAll(TEAMS, COACHES, VENUES, EVENTS);

        assertThat(result.problems()).isEmpty();
        assertThat(result.teams()).hasSize(2);
        assertThat(result.coaches()).hasSize(2);
        assertThat(result.venues()).hasSize(2);
        assertThat(result.events()).hasSize(2);

        Event first = result.events().getFirst();
        assertThat(first.type()).isEqualTo(EventType.PRACTICE);
        assertThat(first.team().name()).isEqualTo("U10 Blue");
        assertThat(first.coach().name()).isEqualTo("Coach Rivera");
        assertThat(first.venue().name()).isEqualTo("Field 2");
        assertThat(first.start())
                .isEqualTo(LocalDateTime.parse("2026-03-03T18:00").atZone(TestData.ZONE));
    }

    @Test
    void rejectsAFileWhoseHeaderIsWrong() {
        String wrongHeader = """
                team_id,name
                T1,U10 Blue
                """;

        ImportResult result = importAll(wrongHeader, COACHES, VENUES, EVENTS);

        assertThat(result.teams()).isEmpty();
        assertThat(result.problems())
                .anySatisfy(problem -> {
                    assertThat(problem.file()).isEqualTo("teams.csv");
                    assertThat(problem.line()).isZero();
                    assertThat(problem.message())
                            .isEqualTo("expected the columns team_id, team_name but found team_id, name");
                });
    }

    @Test
    void rejectsAFileThatIsNotNamedCsv() throws Exception {
        ImportResult result = importer.importAll(
                new CsvUpload("teams.txt", TEAMS.getBytes(UTF_8)),
                upload("coaches.csv", COACHES),
                upload("venues.csv", VENUES),
                upload("events.csv", EVENTS));

        assertThat(result.teams()).isEmpty();
        assertThat(result.problems())
                .anyMatch(problem -> problem.message().equals("only .csv files are accepted"));
    }

    @Test
    void rejectsAFileOverTheByteLimit() {
        ScheduleCsvImporter tinyLimit =
                new ScheduleCsvImporter(TestData.ZONE, new CsvLimits(20, 100));

        ImportResult result = tinyLimit.importAll(
                upload("teams.csv", TEAMS),
                upload("coaches.csv", COACHES),
                upload("venues.csv", VENUES),
                upload("events.csv", EVENTS));

        assertThat(result.teams()).isEmpty();
        assertThat(result.problems())
                .anyMatch(problem -> problem.message().equals("file is larger than the 20 byte limit"));
    }

    @Test
    void rejectsAFileOverTheRowLimit() {
        ScheduleCsvImporter twoRowLimit =
                new ScheduleCsvImporter(TestData.ZONE, new CsvLimits(CsvLimits.DEFAULT.maxBytesPerFile(), 1));

        ImportResult result = twoRowLimit.importAll(
                upload("teams.csv", TEAMS),
                upload("coaches.csv", COACHES),
                upload("venues.csv", VENUES),
                upload("events.csv", EVENTS));

        assertThat(result.problems())
                .anyMatch(problem -> problem.message().equals("file has more than the 1 row limit"));
    }

    @Test
    void rejectsAnEmptyFile() {
        ImportResult result = importAll("", COACHES, VENUES, EVENTS);

        assertThat(result.problems())
                .anyMatch(problem -> problem.message().equals("file is empty"));
    }

    @Test
    void rejectsBytesThatAreNotUtf8() {
        byte[] notUtf8 = {'t', 'e', 'a', 'm', '_', 'i', 'd', ',', 't', 'e', 'a', 'm', '_', 'n', 'a', 'm',
                'e', '\n', 'T', '1', ',', (byte) 0xC3, (byte) 0x28, '\n'};

        ImportResult result = importer.importAll(
                new CsvUpload("teams.csv", notUtf8),
                upload("coaches.csv", COACHES),
                upload("venues.csv", VENUES),
                upload("events.csv", EVENTS));

        assertThat(result.problems())
                .anyMatch(problem -> problem.message().equals("file is not valid UTF-8 text"));
    }

    @Test
    void reportsABadTimestampWithItsLineNumber() {
        String events = """
                event_id,event_type,team_id,coach_id,venue_id,start,end
                E1,practice,T1,C1,V2,2026-03-03T18:00,2026-03-03T19:00
                E2,game,T3,C1,V3,March 3 at 6pm,2026-03-03T20:00
                """;

        ImportResult result = importAll(TEAMS, COACHES, VENUES, events);

        assertThat(result.events()).extracting(Event::id).containsExactly("E1");
        assertThat(result.problems()).singleElement().satisfies(problem -> {
            assertThat(problem.location()).isEqualTo("events.csv line 3");
            assertThat(problem.message())
                    .isEqualTo("start must look like 2026-03-03T18:00, got \"March 3 at 6pm\"");
        });
    }

    @Test
    void reportsAnUnknownReferenceWithItsLineNumber() {
        String events = """
                event_id,event_type,team_id,coach_id,venue_id,start,end
                E1,practice,T1,C9,V2,2026-03-03T18:00,2026-03-03T19:00
                """;

        ImportResult result = importAll(TEAMS, COACHES, VENUES, events);

        assertThat(result.events()).isEmpty();
        assertThat(result.problems()).singleElement().satisfies(problem -> {
            assertThat(problem.location()).isEqualTo("events.csv line 2");
            assertThat(problem.message()).isEqualTo("unknown coach \"C9\"");
        });
    }

    @Test
    void reportsAnEventTypeItDoesNotRecognise() {
        String events = """
                event_id,event_type,team_id,coach_id,venue_id,start,end
                E1,scrimmage,T1,C1,V2,2026-03-03T18:00,2026-03-03T19:00
                """;

        ImportResult result = importAll(TEAMS, COACHES, VENUES, events);

        assertThat(result.events()).isEmpty();
        assertThat(result.problems()).singleElement().satisfies(problem ->
                assertThat(problem.message())
                        .isEqualTo("event_type must be game or practice, got \"scrimmage\""));
    }

    @Test
    void reportsAnEventThatEndsBeforeItStarts() {
        String events = """
                event_id,event_type,team_id,coach_id,venue_id,start,end
                E1,practice,T1,C1,V2,2026-03-03T19:00,2026-03-03T18:00
                """;

        ImportResult result = importAll(TEAMS, COACHES, VENUES, events);

        assertThat(result.events()).isEmpty();
        assertThat(result.problems()).singleElement().satisfies(problem ->
                assertThat(problem.message()).contains("end must be after start"));
    }

    @Test
    void reportsAMissingCell() {
        String events = """
                event_id,event_type,team_id,coach_id,venue_id,start,end
                E1,practice,T1,,V2,2026-03-03T18:00,2026-03-03T19:00
                """;

        ImportResult result = importAll(TEAMS, COACHES, VENUES, events);

        assertThat(result.problems()).singleElement().satisfies(problem ->
                assertThat(problem.message()).isEqualTo("coach_id is missing"));
    }

    @Test
    void reportsDuplicateIdsAndKeepsTheFirst() {
        String teams = """
                team_id,team_name
                T1,U10 Blue
                T1,U10 White
                T3,U12 Red
                """;

        ImportResult result = importAll(teams, COACHES, VENUES, EVENTS);

        assertThat(result.teams()).extracting("name").containsExactly("U10 Blue", "U12 Red");
        assertThat(result.problems()).singleElement().satisfies(problem -> {
            assertThat(problem.location()).isEqualTo("teams.csv line 3");
            assertThat(problem.message()).isEqualTo("duplicate team_id \"T1\"");
        });
    }

    @Test
    void treatsCellContentAsTextOnly() {
        String teams = """
                team_id,team_name
                T1,"=1+1"
                T3,U12 Red
                """;

        ImportResult result = importAll(teams, COACHES, VENUES, EVENTS);

        assertThat(result.problems()).isEmpty();
        assertThat(result.teams()).extracting("name").containsExactly("=1+1", "U12 Red");
    }

    private ImportResult importAll(String teams, String coaches, String venues, String events) {
        return importer.importAll(
                upload("teams.csv", teams),
                upload("coaches.csv", coaches),
                upload("venues.csv", venues),
                upload("events.csv", events));
    }

    private static CsvUpload upload(String fileName, String content) {
        return new CsvUpload(fileName, content.getBytes(UTF_8));
    }
}
