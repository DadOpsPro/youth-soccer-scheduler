package com.dadops.soccer.domain;

public enum EventType {
    GAME("game"),
    PRACTICE("practice");

    private final String label;

    EventType(String label) {
        this.label = label;
    }

    /** Lower-case wording used inside conflict sentences. */
    public String label() {
        return label;
    }
}
