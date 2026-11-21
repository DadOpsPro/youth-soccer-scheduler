package com.dadops.soccer.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import com.dadops.soccer.config.AppProperties.Security;
import com.dadops.soccer.config.AppProperties.Upload;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class SecurityConfigTest {

    private final SecurityConfig config = new SecurityConfig();
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void refusesToBuildTheAccountWithoutAUsername() {
        assertThatIllegalStateException()
                .isThrownBy(() -> config.userDetailsService(properties("", "some-password"), encoder))
                .withMessageContaining("APP_USER must be set");
    }

    @Test
    void refusesToBuildTheAccountWithoutAPassword() {
        assertThatIllegalStateException()
                .isThrownBy(() -> config.userDetailsService(properties("operator", " "), encoder))
                .withMessageContaining("APP_PASSWORD must be set");
    }

    @Test
    void storesTheConfiguredPasswordOnlyAsAHash() {
        UserDetails user = config
                .userDetailsService(properties("operator", "test-only-password"), encoder)
                .loadUserByUsername("operator");

        assertThat(user.getPassword())
                .doesNotContain("test-only-password")
                .startsWith("$2");
        assertThat(encoder.matches("test-only-password", user.getPassword())).isTrue();
    }

    private static AppProperties properties(String username, String password) {
        return new AppProperties(
                "America/New_York", new Upload(1024, 10), new Security(username, password));
    }
}
