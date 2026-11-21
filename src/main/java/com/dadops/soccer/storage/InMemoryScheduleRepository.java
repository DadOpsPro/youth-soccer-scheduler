package com.dadops.soccer.storage;

import com.dadops.soccer.domain.Schedule;
import java.util.concurrent.atomic.AtomicReference;

/** Holds the schedule in memory. Everything is lost when the app restarts. */
public final class InMemoryScheduleRepository implements ScheduleRepository {

    private final AtomicReference<Schedule> schedule = new AtomicReference<>(Schedule.empty());

    @Override
    public Schedule current() {
        return schedule.get();
    }

    @Override
    public void replaceAll(Schedule replacement) {
        schedule.set(replacement);
    }
}
