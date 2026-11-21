package com.dadops.soccer.csv;

/** A file-level problem that stops the whole file from being read. */
public class CsvFormatException extends Exception {

    public CsvFormatException(String message) {
        super(message);
    }

    public CsvFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
