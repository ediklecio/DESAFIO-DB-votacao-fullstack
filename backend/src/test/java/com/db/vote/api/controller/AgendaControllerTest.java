package com.db.vote.api.controller;

import com.db.vote.api.dto.request.CreateAgendaRequest;
import com.db.vote.domain.Agenda;
import com.db.vote.domain.exception.InvalidAgendaException;
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
}
