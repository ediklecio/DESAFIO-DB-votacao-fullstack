package com.db.vote;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Smoke test for springdoc-openapi 2.8.6 running on Spring Boot 4.1.1 / Spring
 * Framework 7 - no certified compatible springdoc release exists yet for this
 * Boot version, so this exists to catch a silent breakage early.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocsTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void shouldServeTheGeneratedOpenApiDocument() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paths./api/v1/agendas").exists())
				.andExpect(jsonPath("$.paths.['/api/v1/agendas/{agendaId}/votes']").exists())
				.andExpect(jsonPath("$.paths.['/api/v1/agendas/{agendaId}/results']").exists());
	}
}
