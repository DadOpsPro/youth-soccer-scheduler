package com.dadops.soccer.csv;

/**
 * Hard caps applied to every uploaded file. Uploads are untrusted, so the
 * parser refuses anything larger rather than trying to cope with it.
 */
public record CsvLimits(long maxBytesPerFile, int maxRowsPerFile) {

    public static final CsvLimits DEFAULT = new CsvLimits(1_048_576L, 2_000);

    public CsvLimits {
        if (maxBytesPerFile <= 0 || maxRowsPerFile <= 0) {
            throw new IllegalArgumentException("limits must be positive");
        }
    }
}
