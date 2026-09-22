package com.db.vote.api.controller;

import com.db.vote.api.dto.request.CreateAgendaRequest;
import com.db.vote.api.dto.response.AgendaOverviewResponse;
import com.db.vote.domain.Agenda;
import com.db.vote.domain.SessionStatus;
import com.db.vote.domain.exception.AgendaNotFoundException;
import com.db.vote.domain.exception.InvalidAgendaException;
import com.db.vote.service.AgendaQueryService;
import com.db.vote.service.AgendaService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AgendaController.class)
class AgendaControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private AgendaService agendaService;

	@MockitoBean
	private AgendaQueryService agendaQueryService;

	@Test
	void shouldReturn201WithLocationAndBodyWhenAgendaIsCreated() throws Exception {
		var request = new CreateAgendaRequest("Reforma do estatuto", "Votação sobre a nova redação do estatuto social");
		var agenda = new Agenda(1L, request.title(), request.description());
		when(agendaService.createAgenda(any(CreateAgendaRequest.class))).thenReturn(agenda);

		mockMvc.perform(post("/api/v1/agendas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/api/v1/agendas/1"))
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.title").value(request.title()))
				.andExpect(jsonPath("$.description").value(request.description()));
	}

	@Test
	void shouldReturn422WhenServiceRejectsAnInvalidAgenda() throws Exception {
		var request = new CreateAgendaRequest("", "Descrição qualquer");
		when(agendaService.createAgenda(any(CreateAgendaRequest.class)))
				.thenThrow(new InvalidAgendaException("Agenda title must not be blank"));

		mockMvc.perform(post("/api/v1/agendas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.message").value("Agenda title must not be blank"));
	}

	@Test
	void shouldReturn200WithTheAgendaOverview() throws Exception {
		var overview = new AgendaOverviewResponse(1L, "Reforma do estatuto", "desc",
				SessionStatus.NOT_STARTED, null, null, 0, 0, 0);
		when(agendaQueryService.getAgenda(1L)).thenReturn(overview);

		mockMvc.perform(get("/api/v1/agendas/{id}", 1L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.sessionStatus").value("NOT_STARTED"))
				.andExpect(jsonPath("$.totalYes").value(0));
	}

	@Test
	void shouldReturn404WhenTheAgendaDoesNotExist() throws Exception {
		when(agendaQueryService.getAgenda(99L)).thenThrow(new AgendaNotFoundException("Agenda 99 not found"));

		mockMvc.perform(get("/api/v1/agendas/{id}", 99L))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Agenda 99 not found"));
	}
}
