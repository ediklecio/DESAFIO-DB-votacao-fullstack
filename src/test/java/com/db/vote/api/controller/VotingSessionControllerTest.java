package com.db.vote.api.controller;

import com.db.vote.api.dto.request.OpenVotingSessionRequest;
import com.db.vote.domain.VotingSession;
import com.db.vote.domain.exception.AgendaNotFoundException;
import com.db.vote.domain.exception.VotingSessionAlreadyExistsException;
import com.db.vote.service.VotingSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VotingSessionController.class)
class VotingSessionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private VotingSessionService votingSessionService;

	@Test
	void shouldReturn201WithSessionDataWhenOpenedWithExplicitDuration() throws Exception {
		var openedAt = LocalDateTime.now();
		var session = new VotingSession(1L, openedAt, 5);
		when(votingSessionService.openSession(eq(1L), eq(5))).thenReturn(session);

		mockMvc.perform(post("/api/v1/agendas/1/sessions")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new OpenVotingSessionRequest(5))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.agendaId").value(1))
				.andExpect(jsonPath("$.closesAt").exists());
	}

	@Test
	void shouldReturn201WhenOpenedWithoutABody() throws Exception {
		var session = new VotingSession(1L, LocalDateTime.now(), 1);
		when(votingSessionService.openSession(eq(1L), isNull())).thenReturn(session);

		mockMvc.perform(post("/api/v1/agendas/1/sessions"))
				.andExpect(status().isCreated());
	}

	@Test
	void shouldReturn404WhenAgendaDoesNotExist() throws Exception {
		when(votingSessionService.openSession(any(), any()))
				.thenThrow(new AgendaNotFoundException("Agenda 1 not found"));

		mockMvc.perform(post("/api/v1/agendas/1/sessions"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Agenda 1 not found"));
	}

	@Test
	void shouldReturn409WhenAgendaAlreadyHasASession() throws Exception {
		when(votingSessionService.openSession(any(), any()))
				.thenThrow(new VotingSessionAlreadyExistsException("Agenda 1 already has a voting session"));

		mockMvc.perform(post("/api/v1/agendas/1/sessions"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Agenda 1 already has a voting session"));
	}
}
