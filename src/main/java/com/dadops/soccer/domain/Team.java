package com.dadops.soccer.domain;

import java.util.Objects;

/** A single youth team, e.g. "U10 Blue". */
public record Team(String id, String name) {

    public Team {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
    }
}
