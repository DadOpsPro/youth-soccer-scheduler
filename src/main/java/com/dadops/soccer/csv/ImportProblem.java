package com.dadops.soccer.csv;

/**
 * Something wrong with an upload, reported back to the user instead of thrown.
 *
 * @param line the 1-based line in the file, or 0 when the problem is with the
 *             file as a whole (wrong headers, too large, and so on)
 */
public record ImportProblem(String file, long line, String message) {

    public static ImportProblem ofFile(String file, String message) {
        return new ImportProblem(file, 0, message);
    }

    public static ImportProblem ofLine(String file, long line, String message) {
        return new ImportProblem(file, line, message);
    }

    /** "events.csv line 4" or "events.csv" for a whole-file problem. */
    public String location() {
        return line > 0 ? file + " line " + line : file;
    }
}
