package com.dadops.soccer.storage;

import com.dadops.soccer.domain.Schedule;

/**
 * Where the loaded schedule lives. Only one schedule exists at a time: an import
 * replaces whatever was there. A database-backed implementation can slot in here
 * without the web or rule code changing.
 */
public interface ScheduleRepository {

    Schedule current();

    void replaceAll(Schedule schedule);
}
