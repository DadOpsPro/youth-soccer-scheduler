package com.dadops.soccer.conflict;

import com.dadops.soccer.domain.Event;
import java.util.List;

/** One scheduling rule. Implementations are plain Java and have no framework ties. */
public interface ConflictRule {

    /** Short name used in the UI and in logs. */
    String name();

    /** Returns every conflict this rule finds in the given events. */
    List<Conflict> detect(List<Event> events);
}
