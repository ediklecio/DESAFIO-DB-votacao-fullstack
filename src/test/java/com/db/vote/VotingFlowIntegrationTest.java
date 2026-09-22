package com.db.vote;

import com.db.vote.api.dto.request.CreateAgendaRequest;
import com.db.vote.api.dto.request.OpenVotingSessionRequest;
import com.db.vote.api.dto.request.RegisterVoteRequest;
import com.db.vote.domain.VoteOption;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end test of the RF01-RF05 flow (agenda -> session -> vote -> result)
 * against a real, JPA-generated H2 schema - not mocks - so the unique
 * constraints backing RN01 and the Agenda/VotingSession cardinality are
 * actually exercised, not just asserted in unit tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VotingFlowIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private static final String ABLE_CPF_1 = "11111111110";
	private static final String ABLE_CPF_2 = "22222222220";

	@Test
	void shouldRunTheFullVotingFlowAndEnforceItsBusinessRules() throws Exception {
		Long agendaId = createAgenda("Reforma do estatuto", "Votação sobre a nova redação");

		// RN04: no session yet -> voting is rejected.
		voteAndExpect(agendaId, 1L, ABLE_CPF_1, VoteOption.YES, status().isUnprocessableEntity());

		// RN06: querying results before any session exists is allowed and reports a closed/zero state.
		mockMvc.perform(get("/api/v1/agendas/{id}/results", agendaId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalYes").value(0))
				.andExpect(jsonPath("$.totalNo").value(0))
				.andExpect(jsonPath("$.sessionClosed").value(true));

		openSession(agendaId, 1);

		// Agenda "1" --> "0..1" VotingSession: a second session on the same agenda is rejected.
		mockMvc.perform(post("/api/v1/agendas/{id}/sessions", agendaId))
				.andExpect(status().isConflict());

		voteAndExpect(agendaId, 1L, ABLE_CPF_1, VoteOption.YES, status().isCreated());
		voteAndExpect(agendaId, 2L, ABLE_CPF_2, VoteOption.NO, status().isCreated());

		// RN01: the same member cannot vote twice - rejected end to end against the real unique constraint.
		voteAndExpect(agendaId, 1L, ABLE_CPF_1, VoteOption.NO, status().isConflict());

		mockMvc.perform(get("/api/v1/agendas/{id}/results", agendaId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.agendaId").value(agendaId))
				.andExpect(jsonPath("$.totalYes").value(1))
				.andExpect(jsonPath("$.totalNo").value(1))
				.andExpect(jsonPath("$.sessionClosed").value(false));
	}

	@Test
	void shouldRejectAVoteWhenTheCpfAlreadyVotedUnderADifferentMemberId() throws Exception {
		Long agendaId = createAgenda("Eleição de diretoria", "Chapa única");
		openSession(agendaId, 1);

		voteAndExpect(agendaId, 1L, ABLE_CPF_1, VoteOption.YES, status().isCreated());

		// RN01 extended: same CPF, different member_id on the same agenda -> rejected.
		voteAndExpect(agendaId, 2L, ABLE_CPF_1, VoteOption.NO, status().isConflict());
	}

	@Test
	void shouldRejectInvalidOrIneligibleCpfBeforeRegisteringTheVote() throws Exception {
		Long agendaId = createAgenda("Prestação de contas", "Aprovação das contas do exercício anterior");
		openSession(agendaId, 1);

		// RF06: not an 11-digit CPF -> the fake client reports it as invalid (404).
		voteAndExpect(agendaId, 1L, "not-a-cpf", VoteOption.YES, status().isNotFound());

		// RN07: valid format but UNABLE_TO_VOTE per the fake client's rule -> 422, never silently discarded.
		voteAndExpect(agendaId, 2L, "11111111111", VoteOption.YES, status().isUnprocessableEntity());
	}

	@Test
	void shouldRejectAnAgendaWithABlankTitle() throws Exception {
		mockMvc.perform(post("/api/v1/agendas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new CreateAgendaRequest(" ", "desc"))))
				.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void shouldReturn404ForOperationsOnAnUnknownAgenda() throws Exception {
		long unknownAgendaId = 999_999L;

		mockMvc.perform(post("/api/v1/agendas/{id}/sessions", unknownAgendaId))
				.andExpect(status().isNotFound());

		voteAndExpect(unknownAgendaId, 1L, ABLE_CPF_1, VoteOption.YES, status().isNotFound());

		mockMvc.perform(get("/api/v1/agendas/{id}/results", unknownAgendaId))
				.andExpect(status().isNotFound());
	}

	private Long createAgenda(String title, String description) throws Exception {
		String responseBody = mockMvc.perform(post("/api/v1/agendas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new CreateAgendaRequest(title, description))))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		JsonNode json = objectMapper.readTree(responseBody);
		return json.get("id").asLong();
	}

	private void openSession(Long agendaId, int durationMinutes) throws Exception {
		mockMvc.perform(post("/api/v1/agendas/{id}/sessions", agendaId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new OpenVotingSessionRequest(durationMinutes))))
				.andExpect(status().isCreated());
	}

	private void voteAndExpect(Long agendaId, Long memberId, String cpf, VoteOption answer,
			org.springframework.test.web.servlet.ResultMatcher expectedStatus) throws Exception {
		mockMvc.perform(post("/api/v1/agendas/{id}/votes", agendaId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new RegisterVoteRequest(memberId, cpf, answer))))
				.andExpect(expectedStatus);
	}
}
