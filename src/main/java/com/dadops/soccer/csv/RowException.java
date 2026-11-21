package com.dadops.soccer.csv;

/** A problem with one row. The importer reports it and moves to the next row. */
class RowException extends Exception {

    RowException(String message) {
        super(message);
    }
}
