# Youth Soccer Multi-Team Schedule Coordinator

A small web app for the person who juggles several youth soccer teams. Load your
schedule from CSV, and it tells you — in plain English — where the schedule
contradicts itself.

This is the first slice. Today it imports CSV, finds coaches and fields that are
booked twice at the same time, and lists what it found on one page. The roadmap
below covers the rest.

## What works now

- Import teams, coaches, fields, and events (games and practices) from four CSV files.
- Detect **coach double-booking**: one coach expected at two overlapping events.
- Detect **field double-booking**: one field used by two overlapping events.
- Explain every conflict as a sentence you can act on, for example:

  > Coach Rivera is booked at Field 2 (U10 Blue practice) and Field 5 (U12 Red game) from 6:30 PM to 7:00 PM on Tue Mar 3.

- Report any file or row it could not read, with the line number, instead of failing the import.

Events that merely touch — one ending exactly when the next begins — are not a
conflict. Travel time between fields is a separate rule that is not built yet.

## Running it

Java 21 and Maven are the only prerequisites.

```bash
cp .env.example .env     # then edit .env and set a real username and password
mvn verify               # compile and run the tests
mvn spring-boot:run      # start on http://localhost:8080
```

Sign in with the `APP_USER` / `APP_PASSWORD` values from your `.env`, then upload
the four files from `sample-data/`. That sample set deliberately contains one
coach overlap and one field double-booking, plus a pair of back-to-back events at
the same field that correctly report nothing.

The app reads `.env` through `spring.config.import` as a convenience for local
work. Anywhere else, set the same names as real environment variables. There is no
default password: if `APP_USER` or `APP_PASSWORD` is missing, start-up fails.

### Configuration

| Variable | Default | Meaning |
| --- | --- | --- |
| `APP_USER` | *(none — required)* | Username for the single operator account |
| `APP_PASSWORD` | *(none — required)* | Password for that account |
| `APP_TIME_ZONE` | `America/New_York` | IANA zone that every CSV timestamp is read in |

Upload limits live in `application.yml`: 1 MB and 2,000 rows per file.

## CSV formats

Four files, each with these exact column headers in this order. Values are
trimmed; surrounding quotes are optional and handled normally. All timestamps use
ISO local date-time — `2026-03-03T18:00` — and are interpreted in the configured
time zone. Times are never read with an offset or zone attached, so one setting
controls the whole schedule.

**teams.csv**

```csv
team_id,team_name
T1,U10 Blue
```

**coaches.csv**

```csv
coach_id,coach_name
C1,Coach Rivera
```

**venues.csv**

```csv
venue_id,venue_name
V2,Field 2
```

**events.csv** — `event_type` is `game` or `practice`; the three id columns must
match rows in the other three files.

```csv
event_id,event_type,team_id,coach_id,venue_id,start,end
E1,practice,T1,C1,V2,2026-03-03T18:00,2026-03-03T19:00
```

A game currently records one team, the one whose schedule you are managing.
Opponents are on the roadmap.

### What gets rejected

A whole file is refused when it is empty, is not named `.csv`, exceeds the size or
row limit, is not valid UTF-8, or has the wrong headers. Individual rows are
skipped — and reported with their line number — when a cell is missing, an id
points at nothing, a timestamp is unparseable, an event ends before it starts, or
an id is duplicated. Everything else in the file still imports.

## Security choices

- **Uploads are untrusted.** Each file is capped at 1 MB (enforced by both the
  servlet container and the parser) and 2,000 rows. Only `.csv` names are accepted,
  headers must match exactly, and bytes are decoded as strict UTF-8 so mangled or
  binary input is refused rather than silently mangled.
- **Nothing is evaluated.** Cell values are only ever read as strings. No cell is
  treated as a formula, expression, or file path. A cell containing `=1+1` imports
  as the literal text `=1+1` (there is a test for this).
- **Bad input reports, it does not crash.** A bad row produces a line-numbered
  message on the page; the rest of the file still imports.
- **Output is escaped.** Thymeleaf's default escaping is used everywhere;
  `th:utext` appears nowhere in this repository. Values echoed back from a file —
  including the client-supplied file name — are length-capped first.
- **Authentication and CSRF.** Spring Security guards every route with form login
  for one local account read from the environment. The password is bcrypt-hashed
  into memory at start-up and never written anywhere. CSRF protection is on, and
  the upload form carries the token. Start-up fails loudly if credentials are
  absent, rather than falling back to a generated password.
- **Response headers.** `Content-Security-Policy: default-src 'none'` with styles
  and images limited to `'self'` — the app ships no JavaScript at all, so scripts
  are forbidden outright rather than allowed inline. Plus `X-Content-Type-Options:
  nosniff`, `X-Frame-Options: DENY`, `frame-ancestors 'none'`, and
  `Referrer-Policy: no-referrer`.
- **Errors reveal nothing.** Stack traces, exception types, and messages are off
  in `application.yml`; a custom error page shows only the status code.
- **No secrets in the repo.** `.gitignore` covers `.env`, IDE files, and build
  output. `.env.example` holds placeholders only.
- **Dependencies.** `.github/dependabot.yml` opens weekly Maven update PRs. The
  OWASP dependency-check plugin is wired into an opt-in profile so the everyday
  build stays fast and offline-capable:
  `mvn -Psecurity-scan verify -Dnvd.api.key=YOUR_KEY` (the NVD feed needs a free
  API key; without one the scan is slow and rate-limited).
- **All data is synthetic.** Every team, coach, and field in `sample-data/` and in
  the tests is invented. No real club, team, coach, player, or family data belongs
  in this repository.

## How the code is laid out

```
domain/     Team, Coach, Venue, Event, Schedule — plain records, no framework
conflict/   ConflictRule and one small class per rule — plain Java, no Spring
csv/        Upload validation and parsing into the domain model
storage/    ScheduleRepository interface + the in-memory implementation
web/        The one controller, the login page, and the view row for the table
config/     Spring wiring, settings binding, and the security filter chain
```

`conflict/` and `domain/` have no Spring imports, which is what makes the rules
quick to read and quick to unit test. Storage is in memory behind
`ScheduleRepository`, so a database can be added without touching the rules or
the web layer. Nothing persists across a restart yet.

### Dependencies, and why

Spring Boot web, Thymeleaf, and Security; Apache Commons CSV for parsing; JUnit 5
and AssertJ for tests, plus `spring-security-test` so the MockMvc tests can
exercise login and CSRF. Nothing else.

## Roadmap

- [ ] **Travel and buffer time** between back-to-back events at different venues
      (needs a location or a venue-to-venue travel matrix).
- [ ] **Blackout dates** — flag any event on a date the club has closed.
- [ ] **Suggested fixes** — open slots and swaps that would resolve a conflict.
- [ ] **Export** per-team and per-coach schedules as CSV and iCal (`.ics`).
      CSV export must prefix cells starting with `=`, `+`, `-`, `@`, tab, or
      carriage return, so a spreadsheet never treats a cell as a formula.
- [ ] **Opponents on games** — a second team per game, rather than one.
- [ ] **Persistence** — a real database behind `ScheduleRepository`.
- [ ] **CI** — run `mvn verify` on every push.
- [ ] **Constraint solver** that generates a conflict-free draft schedule.

## License

MIT — see [LICENSE](LICENSE). A solely owned personal project; see
[CONTRIBUTING.md](CONTRIBUTING.md).
