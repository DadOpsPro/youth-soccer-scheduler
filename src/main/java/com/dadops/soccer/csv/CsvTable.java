package com.dadops.soccer.csv;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.DuplicateHeaderMode;

/**
 * A validated CSV file: correct extension, within the size and row limits,
 * decodable as UTF-8, and carrying exactly the expected columns.
 *
 * <p>Cell values are only ever read as strings. Nothing here interprets a cell
 * as a formula, expression, or path.
 */
public final class CsvTable {

    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setDuplicateHeaderMode(DuplicateHeaderMode.DISALLOW)
            .setAllowMissingColumnNames(false)
            .setIgnoreEmptyLines(true)
            .setIgnoreSurroundingSpaces(true)
            .setTrim(true)
            .get();

    private final String fileName;
    private final List<CSVRecord> rows;

    private CsvTable(String fileName, List<CSVRecord> rows) {
        this.fileName = fileName;
        this.rows = rows;
    }

    public String fileName() {
        return fileName;
    }

    public List<CSVRecord> rows() {
        return rows;
    }

    public static CsvTable read(CsvUpload upload, List<String> expectedHeaders, CsvLimits limits)
            throws CsvFormatException {
        requireCsvExtension(upload.fileName());
        requireWithinLimits(upload, limits);

        String text = decodeUtf8(upload.content());
        try (CSVParser parser = CSVParser.parse(new StringReader(text), FORMAT)) {
            requireExpectedHeaders(parser.getHeaderNames(), expectedHeaders);
            return new CsvTable(upload.fileName(), readRows(parser, limits));
        } catch (IllegalArgumentException | IOException e) {
            // Commons CSV reports malformed quoting and duplicate headers this way.
            throw new CsvFormatException("could not be read as CSV: " + e.getMessage(), e);
        }
    }

    private static void requireCsvExtension(String fileName) throws CsvFormatException {
        if (!fileName.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new CsvFormatException("only .csv files are accepted");
        }
    }

    private static void requireWithinLimits(CsvUpload upload, CsvLimits limits) throws CsvFormatException {
        if (upload.size() == 0) {
            throw new CsvFormatException("file is empty");
        }
        if (upload.size() > limits.maxBytesPerFile()) {
            throw new CsvFormatException("file is larger than the %d byte limit"
                    .formatted(limits.maxBytesPerFile()));
        }
    }

    /** Strict decoding so mangled or binary uploads are rejected, not silently replaced. */
    private static String decodeUtf8(byte[] content) throws CsvFormatException {
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            return decoder.decode(ByteBuffer.wrap(content)).toString();
        } catch (CharacterCodingException e) {
            throw new CsvFormatException("file is not valid UTF-8 text", e);
        }
    }

    private static void requireExpectedHeaders(List<String> actual, List<String> expected)
            throws CsvFormatException {
        if (!actual.equals(expected)) {
            throw new CsvFormatException("expected the columns %s but found %s"
                    .formatted(String.join(", ", expected), String.join(", ", actual)));
        }
    }

    private static List<CSVRecord> readRows(CSVParser parser, CsvLimits limits) throws CsvFormatException {
        List<CSVRecord> rows = new ArrayList<>();
        for (CSVRecord row : parser) {
            if (rows.size() == limits.maxRowsPerFile()) {
                throw new CsvFormatException("file has more than the %d row limit"
                        .formatted(limits.maxRowsPerFile()));
            }
            rows.add(row);
        }
        return rows;
    }
}
