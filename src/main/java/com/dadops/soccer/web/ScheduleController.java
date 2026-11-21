package com.dadops.soccer.web;

import com.dadops.soccer.conflict.ConflictDetector;
import com.dadops.soccer.conflict.ConflictRule;
import com.dadops.soccer.csv.CsvUpload;
import com.dadops.soccer.csv.ImportProblem;
import com.dadops.soccer.csv.ImportResult;
import com.dadops.soccer.csv.ScheduleCsvImporter;
import com.dadops.soccer.domain.Event;
import com.dadops.soccer.domain.Schedule;
import com.dadops.soccer.storage.ScheduleRepository;
import java.io.IOException;
import java.security.Principal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/** The one page: upload four CSVs, then read the conflicts found in them. */
@Controller
public class ScheduleController {

    private static final int MAX_FILE_NAME_LENGTH = 100;

    private final ScheduleRepository repository;
    private final ScheduleCsvImporter importer;
    private final ConflictDetector detector;
    private final ZoneId zone;

    public ScheduleController(
            ScheduleRepository repository,
            ScheduleCsvImporter importer,
            ConflictDetector detector,
            ZoneId schedulingZone) {
        this.repository = repository;
        this.importer = importer;
        this.detector = detector;
        this.zone = schedulingZone;
    }

    /** Available to every page in this controller, so the header can name the operator. */
    @ModelAttribute("currentUser")
    public String currentUser(Principal principal) {
        return principal == null ? null : principal.getName();
    }

    @GetMapping("/")
    public String index(Model model) {
        render(model, repository.current(), List.of());
        return "index";
    }

    @PostMapping("/import")
    public String importSchedule(
            @RequestParam("teams") MultipartFile teams,
            @RequestParam("coaches") MultipartFile coaches,
            @RequestParam("venues") MultipartFile venues,
            @RequestParam("events") MultipartFile events,
            Model model) {

        List<ImportProblem> problems = new ArrayList<>();
        CsvUpload teamsUpload = toUpload(teams, "teams.csv", problems);
        CsvUpload coachesUpload = toUpload(coaches, "coaches.csv", problems);
        CsvUpload venuesUpload = toUpload(venues, "venues.csv", problems);
        CsvUpload eventsUpload = toUpload(events, "events.csv", problems);

        // All four files are needed to build events, so a missing one stops the import
        // and leaves the previously loaded schedule untouched.
        if (!problems.isEmpty()) {
            render(model, repository.current(), problems);
            return "index";
        }

        ImportResult result = importer.importAll(teamsUpload, coachesUpload, venuesUpload, eventsUpload);
        Schedule schedule =
                new Schedule(result.teams(), result.coaches(), result.venues(), result.events());
        repository.replaceAll(schedule);

        model.addAttribute("imported", true);
        render(model, schedule, result.problems());
        return "index";
    }

    private void render(Model model, Schedule schedule, List<ImportProblem> problems) {
        model.addAttribute("schedule", schedule);
        model.addAttribute("eventRows", schedule.events().stream()
                .sorted(Comparator.comparing(Event::start).thenComparing(Event::id))
                .map(EventRow::of)
                .toList());
        model.addAttribute("conflicts", detector.detect(schedule.events()));
        model.addAttribute("problems", problems);
        model.addAttribute("ruleNames", detector.rules().stream().map(ConflictRule::name).toList());
        model.addAttribute("zone", zone.getId());
    }

    /** Returns null and records a problem when the field was left empty or unreadable. */
    private CsvUpload toUpload(MultipartFile file, String expectedName, List<ImportProblem> problems) {
        if (file == null || file.isEmpty()) {
            problems.add(ImportProblem.ofFile(expectedName, "no file was chosen"));
            return null;
        }
        String name = displayName(file, expectedName);
        try {
            return new CsvUpload(name, file.getBytes());
        } catch (IOException e) {
            problems.add(ImportProblem.ofFile(name, "upload could not be read"));
            return null;
        }
    }

    /**
     * The client-supplied name is only ever shown back to the user and checked for a
     * .csv ending, never used as a path, so any directory part is stripped. An absurdly
     * long name is replaced outright rather than truncated, which would mangle the
     * extension the parser checks.
     */
    private static String displayName(MultipartFile file, String fallback) {
        String name = StringUtils.getFilename(file.getOriginalFilename());
        if (name == null || name.isBlank() || name.length() > MAX_FILE_NAME_LENGTH) {
            return fallback;
        }
        return name;
    }
}
