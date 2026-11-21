package com.dadops.soccer.domain;

import java.util.Objects;

/** A field or facility where events happen, e.g. "Field 2". */
public record Venue(String id, String name) {

    public Venue {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
    }
}
