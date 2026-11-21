package com.dadops.soccer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Settings under the {@code app} prefix. Credentials come from the environment
 * (see {@code .env.example}); nothing sensitive has a default.
 *
 * @param timeZone IANA zone id that every CSV timestamp is interpreted in
 */
@ConfigurationProperties("app")
public record AppProperties(String timeZone, Upload upload, Security security) {

    /** @param maxBytesPerFile per-file upload cap, also enforced by the servlet container */
    public record Upload(long maxBytesPerFile, int maxRowsPerFile) {
    }

    public record Security(String username, String password) {
    }
}
