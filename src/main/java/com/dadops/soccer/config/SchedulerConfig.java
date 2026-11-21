package com.dadops.soccer.config;

import com.dadops.soccer.conflict.CoachDoubleBookingRule;
import com.dadops.soccer.conflict.ConflictDetector;
import com.dadops.soccer.conflict.VenueDoubleBookingRule;
import com.dadops.soccer.csv.CsvLimits;
import com.dadops.soccer.csv.ScheduleCsvImporter;
import com.dadops.soccer.storage.InMemoryScheduleRepository;
import com.dadops.soccer.storage.ScheduleRepository;
import java.time.ZoneId;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the plain-Java pieces into Spring. The rules and importer know nothing about Spring. */
@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class SchedulerConfig {

    /** Throws at startup if the configured zone id is not a real zone. */
    @Bean
    public ZoneId schedulingZone(AppProperties properties) {
        return ZoneId.of(properties.timeZone());
    }

    @Bean
    public CsvLimits csvLimits(AppProperties properties) {
        return new CsvLimits(properties.upload().maxBytesPerFile(), properties.upload().maxRowsPerFile());
    }

    @Bean
    public ScheduleCsvImporter scheduleCsvImporter(ZoneId schedulingZone, CsvLimits csvLimits) {
        return new ScheduleCsvImporter(schedulingZone, csvLimits);
    }

    @Bean
    public ConflictDetector conflictDetector() {
        return new ConflictDetector(List.of(new CoachDoubleBookingRule(), new VenueDoubleBookingRule()));
    }

    @Bean
    public ScheduleRepository scheduleRepository() {
        return new InMemoryScheduleRepository();
    }
}
