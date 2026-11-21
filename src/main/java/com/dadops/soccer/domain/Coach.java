package com.dadops.soccer.domain;

import java.util.Objects;

/** A coach who can be assigned to events. */
public record Coach(String id, String name) {

    public Coach {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
    }
}
