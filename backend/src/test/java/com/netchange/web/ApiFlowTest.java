package com.netchange.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netchange.repo.AuditEventRepository;
import com.netchange.repo.ChangeRequestRepository;
import com.netchange.repo.DeviceExecutionRepository;
import com.netchange.repo.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiFlowTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired DeviceRepository devices;
    @Autowired ChangeRequestRepository changes;
    @Autowired DeviceExecutionRepository executions;
    @Autowired AuditEventRepository audit;

    @BeforeEach
    void clean() {
        audit.deleteAll();
        executions.deleteAll();
        changes.deleteAll();
        devices.deleteAll();
    }

    private void registerDevice(String name) throws Exception {
        mvc.perform(post("/api/devices").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"%s","ipAddress":"10.0.0.1","model":"Catalyst 9300","osVersion":"17.9.4",
                 "site":"Lab","deviceGroup":"edge","runningConfig":"hostname %s"}
                """.formatted(name, name))).andExpect(status().isCreated());
    }

    @Test
    void fullChangeFlowOverRest() throws Exception {
        registerDevice("sw-a");
        registerDevice("sw-b");

        String created = mvc.perform(post("/api/changes").header("X-User", "alice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"NTP update","reason":"New server","targetGroup":"edge",
                                 "configLines":"ntp server 10.9.9.9","batchSize":1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"))
                .andReturn().getResponse().getContentAsString();
        long id = json.readTree(created).get("id").asLong();

        mvc.perform(post("/api/changes/{id}/approve", id).header("X-User", "alice"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/changes/{id}/deploy", id).header("X-User", "alice"))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/changes/{id}/approve", id).header("X-User", "bob"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mvc.perform(get("/api/changes/{id}/dry-run", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deployable").value(true))
                .andExpect(jsonPath("$.devices[0].stage").value("CANARY"))
                .andExpect(jsonPath("$.devices[0].added[0]").value("ntp server 10.9.9.9"));

        mvc.perform(post("/api/changes/{id}/deploy", id).header("X-User", "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.change.status").value("DEPLOYED"))
                .andExpect(jsonPath("$.executions.length()").value(2));

        mvc.perform(post("/api/changes/{id}/rollback", id).header("X-User", "carol"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.change.status").value("ROLLED_BACK"));

        mvc.perform(get("/api/changes/{id}/audit", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("CREATED"));
    }

    @Test
    void validationAndMissingResourcesReturnCleanErrors() throws Exception {
        mvc.perform(post("/api/changes").header("X-User", "alice")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/changes/999")).andExpect(status().isNotFound());
        mvc.perform(post("/api/changes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"t\",\"targetGroup\":\"g\",\"configLines\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }
}
