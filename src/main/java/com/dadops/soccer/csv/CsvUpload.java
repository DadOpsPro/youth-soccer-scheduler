package com.dadops.soccer.csv;

import java.util.Objects;

/**
 * One uploaded file, already read into memory. Keeping this a plain record lets
 * the import code stay free of web-framework types.
 */
public record CsvUpload(String fileName, byte[] content) {

    public CsvUpload {
        Objects.requireNonNull(fileName, "fileName");
        Objects.requireNonNull(content, "content");
    }

    public int size() {
        return content.length;
    }
}
