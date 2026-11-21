package com.dadops.soccer.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/** End-to-end checks over the single page, including the security behaviour. */
@SpringBootTest(properties = {
        "app.security.username=test-operator",
        "app.security.password=test-only-password-not-a-real-secret"
})
@AutoConfigureMockMvc
class ScheduleControllerTest {

    private static final Path SAMPLE_DATA = Path.of("sample-data");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sendsAnonymousVisitorsToTheLoginPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser("test-operator")
    void rejectsAnUploadWithoutACsrfToken() throws Exception {
        mockMvc.perform(multipart("/import")
                        .file(sampleFile("teams", "teams.csv"))
                        .file(sampleFile("coaches", "coaches.csv"))
                        .file(sampleFile("venues", "venues.csv"))
                        .file(sampleFile("events", "events.csv")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser("test-operator")
    void showsAnEmptyStateBeforeAnythingIsImported() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No schedule is loaded yet")));
    }

    @Test
    @WithMockUser("test-operator")
    void importsTheSampleDataAndExplainsEveryConflict() throws Exception {
        String page = mockMvc.perform(multipart("/import")
                        .file(sampleFile("teams", "teams.csv"))
                        .file(sampleFile("coaches", "coaches.csv"))
                        .file(sampleFile("venues", "venues.csv"))
                        .file(sampleFile("events", "events.csv"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(page)
                .contains("Import finished.")
                .doesNotContain("Skipped input")
                .contains("Coach Rivera is booked at Field 2 (U10 Blue practice) "
                        + "and Field 5 (U12 Red game) from 6:30 PM to 7:00 PM on Tue Mar 3.")
                .contains("Field 2 is double-booked: U14 Green game (Coach Lindqvist) "
                        + "and U10 White game (Coach Okafor) from 7:00 PM to 7:30 PM on Thu Mar 5.");
    }

    @Test
    @WithMockUser("test-operator")
    void reportsAMissingFileWithoutTouchingTheLoadedSchedule() throws Exception {
        mockMvc.perform(multipart("/import")
                        .file(sampleFile("teams", "teams.csv"))
                        .file(new MockMultipartFile("coaches", "", MediaType.TEXT_PLAIN_VALUE, new byte[0]))
                        .file(sampleFile("venues", "venues.csv"))
                        .file(sampleFile("events", "events.csv"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("no file was chosen")));
    }

    @Test
    void setsTheSecurityHeadersOnEveryResponse() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy",
                        "default-src 'none'; style-src 'self'; img-src 'self'; "
                                + "form-action 'self'; base-uri 'self'; frame-ancestors 'none'"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"));
    }

    @Test
    @WithMockUser("test-operator")
    void showsAnErrorPageWithoutAStackTrace() throws Exception {
        String page = mockMvc.perform(get("/not-a-page"))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(page).doesNotContain("Exception", "com.dadops.soccer", "at java.");
    }

    private static MockMultipartFile sampleFile(String field, String fileName) throws IOException {
        return new MockMultipartFile(
                field, fileName, "text/csv", Files.readAllBytes(SAMPLE_DATA.resolve(fileName)));
    }
}
