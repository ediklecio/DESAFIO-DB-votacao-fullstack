package com.db.vote.api.controller;

import com.db.vote.api.dto.request.RegisterVoteRequest;
import com.db.vote.api.dto.response.VotingResultResponse;
import com.db.vote.domain.Vote;
import com.db.vote.domain.VoteOption;
import com.db.vote.domain.exception.AgendaNotFoundException;
import com.db.vote.domain.exception.DuplicateVoteException;
import com.db.vote.domain.exception.InvalidCpfException;
import com.db.vote.domain.exception.UnableToVoteException;
import com.db.vote.domain.exception.VotingSessionClosedException;
import com.db.vote.service.VoteService;
import com.db.vote.service.VotingResultService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

	@MockitoBean
	private VotingResultService votingResultService;

	@Test
	void shouldReturn201WhenVoteIsRegistered() throws Exception {
		var request = new RegisterVoteRequest(42L, "11111111110", VoteOption.YES);
		var vote = new Vote(1L, 1L, 42L, VoteOption.YES, LocalDateTime.now());
		when(voteService.registerVote(eq(1L), eq(42L), eq("11111111110"), eq(VoteOption.YES))).thenReturn(vote);

		mockMvc.perform(post("/api/v1/agendas/1/votes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.memberId").value(42))
				.andExpect(jsonPath("$.voteAnswer").value("YES"));
	}

	@Test
	void shouldReturn404WhenAgendaNotFound() throws Exception {
		when(voteService.registerVote(any(), any(), any(), any()))
				.thenThrow(new AgendaNotFoundException("Agenda 1 not found"));

		mockMvc.perform(post("/api/v1/agendas/1/votes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new RegisterVoteRequest(42L, "11111111110", VoteOption.YES))))
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldReturn422WhenSessionIsClosed() throws Exception {
		when(voteService.registerVote(any(), any(), any(), any()))
				.thenThrow(new VotingSessionClosedException("Voting session for agenda 1 is not open"));

		mockMvc.perform(post("/api/v1/agendas/1/votes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new RegisterVoteRequest(42L, "11111111110", VoteOption.YES))))
				.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void shouldReturn409WhenVoteIsDuplicate() throws Exception {
		when(voteService.registerVote(any(), any(), any(), any()))
				.thenThrow(new DuplicateVoteException("Member 42 already voted on agenda 1"));

		mockMvc.perform(post("/api/v1/agendas/1/votes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new RegisterVoteRequest(42L, "11111111110", VoteOption.YES))))
				.andExpect(status().isConflict());
	}

	@Test
	void shouldReturn404WhenCpfIsInvalid() throws Exception {
		when(voteService.registerVote(any(), any(), any(), any()))
				.thenThrow(new InvalidCpfException("CPF invalid is not a valid CPF"));

		mockMvc.perform(post("/api/v1/agendas/1/votes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new RegisterVoteRequest(42L, "invalid", VoteOption.YES))))
				.andExpect(status().isNotFound());
	}

	@Test
	void shouldReturn422WhenMemberIsUnableToVote() throws Exception {
		when(voteService.registerVote(any(), any(), any(), any()))
				.thenThrow(new UnableToVoteException("Member 42 is not able to vote"));

		mockMvc.perform(post("/api/v1/agendas/1/votes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new RegisterVoteRequest(42L, "11111111111", VoteOption.YES))))
				.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void shouldReturn200WithTheVotingResult() throws Exception {
		when(votingResultService.getResult(1L)).thenReturn(new VotingResultResponse(1L, 7, 3, false));

		mockMvc.perform(get("/api/v1/agendas/1/results"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalYes").value(7))
				.andExpect(jsonPath("$.totalNo").value(3))
				.andExpect(jsonPath("$.sessionClosed").value(false));
	}

	@Test
	void shouldReturn404WhenQueryingResultOfAnUnknownAgenda() throws Exception {
		when(votingResultService.getResult(1L)).thenThrow(new AgendaNotFoundException("Agenda 1 not found"));

		mockMvc.perform(get("/api/v1/agendas/1/results"))
				.andExpect(status().isNotFound());
	}
}
