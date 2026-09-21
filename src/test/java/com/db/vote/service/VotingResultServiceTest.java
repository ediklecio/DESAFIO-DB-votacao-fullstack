package com.db.vote.service;

import com.db.vote.api.dto.response.VotingResultResponse;
import com.db.vote.domain.Agenda;
import com.db.vote.domain.VoteOption;
import com.db.vote.domain.exception.AgendaNotFoundException;
import com.db.vote.repository.AgendaRepository;
import com.db.vote.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VotingResultServiceTest {

	private static final Long AGENDA_ID = 1L;

	@Mock
	private VoteRepository voteRepository;

	@Mock
	private VotingSessionService votingSessionService;

	@Mock
	private AgendaRepository agendaRepository;

	private VotingResultService votingResultService;

	@BeforeEach
	void setUp() {
		votingResultService = new VotingResultService(voteRepository, votingSessionService, agendaRepository);
	}

	@Test
	void shouldCountYesAndNoVotesAndFlagAnOpenSessionAsNotClosed() {
		when(agendaRepository.findById(AGENDA_ID)).thenReturn(Optional.of(new Agenda(AGENDA_ID, "Reforma do estatuto", null)));
		when(voteRepository.countByAgendaIdAndVoteAnswer(AGENDA_ID, VoteOption.YES)).thenReturn(7L);
		when(voteRepository.countByAgendaIdAndVoteAnswer(AGENDA_ID, VoteOption.NO)).thenReturn(3L);
		when(votingSessionService.isSessionOpen(AGENDA_ID)).thenReturn(true);

		VotingResultResponse result = votingResultService.getResult(AGENDA_ID);

		assertThat(result.agendaId()).isEqualTo(AGENDA_ID);
		assertThat(result.totalYes()).isEqualTo(7L);
		assertThat(result.totalNo()).isEqualTo(3L);
		assertThat(result.sessionClosed()).isFalse();
	}

	@Test
	void shouldFlagResultAsClosedWhenTheSessionIsNoLongerOpen() {
		when(agendaRepository.findById(AGENDA_ID)).thenReturn(Optional.of(new Agenda(AGENDA_ID, "Reforma do estatuto", null)));
		when(voteRepository.countByAgendaIdAndVoteAnswer(AGENDA_ID, VoteOption.YES)).thenReturn(1L);
		when(voteRepository.countByAgendaIdAndVoteAnswer(AGENDA_ID, VoteOption.NO)).thenReturn(0L);
		when(votingSessionService.isSessionOpen(AGENDA_ID)).thenReturn(false);

		VotingResultResponse result = votingResultService.getResult(AGENDA_ID);

		assertThat(result.sessionClosed()).isTrue();
	}

	@Test
	void shouldThrowAgendaNotFoundExceptionWhenAgendaDoesNotExist() {
		when(agendaRepository.findById(AGENDA_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> votingResultService.getResult(AGENDA_ID))
				.isInstanceOf(AgendaNotFoundException.class);
	}
}
