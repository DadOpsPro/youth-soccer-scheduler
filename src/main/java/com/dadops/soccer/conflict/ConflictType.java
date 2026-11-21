package com.dadops.soccer.conflict;

public enum ConflictType {
    COACH_DOUBLE_BOOKED("Coach double-booked"),
    VENUE_DOUBLE_BOOKED("Field double-booked");

    private final String label;

    ConflictType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
