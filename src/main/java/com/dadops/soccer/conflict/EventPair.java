package com.dadops.soccer.conflict;

import com.dadops.soccer.domain.Event;

/** Two events that a rule found a problem between, earliest start first. */
public record EventPair(Event earlier, Event later) {
}
