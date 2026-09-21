package com.db.vote.api.controller;

import com.db.vote.api.dto.request.RegisterVoteRequest;
import com.db.vote.domain.Vote;
import com.db.vote.domain.VoteOption;
import com.db.vote.domain.exception.AgendaNotFoundException;
import com.db.vote.domain.exception.DuplicateVoteException;
import com.db.vote.domain.exception.VotingSessionClosedException;
import com.db.vote.service.VoteService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VoteController.class)
class VoteControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private VoteService voteService;

	@Test
	void shouldReturn201WhenVoteIsRegistered() throws Exception {
		var request = new RegisterVoteRequest(42L, VoteOption.YES);
		var vote = new Vote(1L, 1L, 42L, VoteOption.YES, LocalDateTime.now());
		when(voteService.registerVote(eq(1L), eq(42L), eq(VoteOption.YES))).thenReturn(vote);

		mockMvc.perform(post("/api/v1/agendas/1/votes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.memberId").value(42))
				.andExpect(jsonPath("$.voteAnswer").value("YES"));
	}

	@Test
	void shouldReturn404WhenAgendaNotFound() throws Exception {
		when(voteService.registerVote(any(), any(), any()))
				.thenThrow(new AgendaNotFoundException("Agenda 1 not found"));

		mockMvc.perform(post("/api/v1/agendas/1/votes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new RegisterVoteRequest(42L, VoteOption.YES))))
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldReturn422WhenSessionIsClosed() throws Exception {
		when(voteService.registerVote(any(), any(), any()))
				.thenThrow(new VotingSessionClosedException("Voting session for agenda 1 is not open"));

		mockMvc.perform(post("/api/v1/agendas/1/votes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new RegisterVoteRequest(42L, VoteOption.YES))))
				.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void shouldReturn409WhenVoteIsDuplicate() throws Exception {
		when(voteService.registerVote(any(), any(), any()))
				.thenThrow(new DuplicateVoteException("Member 42 already voted on agenda 1"));

		mockMvc.perform(post("/api/v1/agendas/1/votes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new RegisterVoteRequest(42L, VoteOption.YES))))
				.andExpect(status().isConflict());
	}
}
