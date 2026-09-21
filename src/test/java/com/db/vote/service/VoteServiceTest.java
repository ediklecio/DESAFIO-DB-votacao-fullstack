package com.db.vote.service;

import com.db.vote.domain.Agenda;
import com.db.vote.domain.Vote;
import com.db.vote.domain.VoteOption;
import com.db.vote.domain.exception.AgendaNotFoundException;
import com.db.vote.domain.exception.DuplicateVoteException;
import com.db.vote.domain.exception.InvalidCpfException;
import com.db.vote.domain.exception.UnableToVoteException;
import com.db.vote.domain.exception.VotingSessionClosedException;
import com.db.vote.infra.client.CpfValidationClient;
import com.db.vote.infra.client.VotingAbilityResponse;
import com.db.vote.infra.client.VotingAbilityStatus;
import com.db.vote.repository.AgendaRepository;
import com.db.vote.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

	private static final Long AGENDA_ID = 1L;
	private static final Long MEMBER_ID = 42L;
	private static final String ABLE_CPF = "11111111110";
	private static final String UNABLE_CPF = "11111111111";

	@Mock
	private VoteRepository voteRepository;

	@Mock
	private VotingSessionService votingSessionService;

	@Mock
	private AgendaRepository agendaRepository;

	@Mock
	private CpfValidationClient cpfValidationClient;

	private VoteService voteService;

	@BeforeEach
	void setUp() {
		voteService = new VoteService(voteRepository, votingSessionService, agendaRepository, cpfValidationClient);
	}

	@Nested
	@DisplayName("When registering a vote")
	class WhenRegisteringAVote {

		@Test
		@DisplayName("should persist the vote when the CPF is able to vote, the session is open and the member has not voted yet")
		void shouldRegisterVote() {
			when(agendaRepository.findById(AGENDA_ID)).thenReturn(Optional.of(new Agenda(AGENDA_ID, "Reforma do estatuto", null)));
			when(cpfValidationClient.checkVotingAbility(ABLE_CPF)).thenReturn(new VotingAbilityResponse(VotingAbilityStatus.ABLE_TO_VOTE));
			when(votingSessionService.isSessionOpen(AGENDA_ID)).thenReturn(true);
			when(voteRepository.existsByAgendaIdAndMemberId(AGENDA_ID, MEMBER_ID)).thenReturn(false);
			when(voteRepository.save(any(Vote.class))).thenAnswer(invocation -> invocation.getArgument(0));

			Vote result = voteService.registerVote(AGENDA_ID, MEMBER_ID, ABLE_CPF, VoteOption.YES);

			ArgumentCaptor<Vote> captor = ArgumentCaptor.forClass(Vote.class);
			verify(voteRepository).save(captor.capture());
			assertThat(captor.getValue().getAgendaId()).isEqualTo(AGENDA_ID);
			assertThat(captor.getValue().getMemberId()).isEqualTo(MEMBER_ID);
			assertThat(captor.getValue().getVoteAnswer()).isEqualTo(VoteOption.YES);
			assertThat(result.getVoteAnswer()).isEqualTo(VoteOption.YES);
		}

		@Test
		@DisplayName("should throw AgendaNotFoundException when the agenda does not exist")
		void shouldThrowWhenAgendaDoesNotExist() {
			when(agendaRepository.findById(AGENDA_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> voteService.registerVote(AGENDA_ID, MEMBER_ID, ABLE_CPF, VoteOption.YES))
					.isInstanceOf(AgendaNotFoundException.class);
			verify(voteRepository, never()).save(any());
		}

		@Test
		@DisplayName("should propagate InvalidCpfException from the CPF client (RF06/RN07)")
		void shouldPropagateInvalidCpf() {
			when(agendaRepository.findById(AGENDA_ID)).thenReturn(Optional.of(new Agenda(AGENDA_ID, "Reforma do estatuto", null)));
			when(cpfValidationClient.checkVotingAbility("invalid")).thenThrow(new InvalidCpfException("CPF invalid is not a valid CPF"));

			assertThatThrownBy(() -> voteService.registerVote(AGENDA_ID, MEMBER_ID, "invalid", VoteOption.YES))
					.isInstanceOf(InvalidCpfException.class);
			verify(voteRepository, never()).save(any());
		}

		@Test
		@DisplayName("should throw UnableToVoteException when the CPF client reports UNABLE_TO_VOTE (RN07)")
		void shouldThrowWhenCpfIsUnableToVote() {
			when(agendaRepository.findById(AGENDA_ID)).thenReturn(Optional.of(new Agenda(AGENDA_ID, "Reforma do estatuto", null)));
			when(cpfValidationClient.checkVotingAbility(UNABLE_CPF)).thenReturn(new VotingAbilityResponse(VotingAbilityStatus.UNABLE_TO_VOTE));

			assertThatThrownBy(() -> voteService.registerVote(AGENDA_ID, MEMBER_ID, UNABLE_CPF, VoteOption.YES))
					.isInstanceOf(UnableToVoteException.class);
			verify(voteRepository, never()).save(any());
		}

		@Test
		@DisplayName("should throw VotingSessionClosedException when there is no open session (RN02/RN04)")
		void shouldThrowWhenSessionIsNotOpen() {
			when(agendaRepository.findById(AGENDA_ID)).thenReturn(Optional.of(new Agenda(AGENDA_ID, "Reforma do estatuto", null)));
			when(cpfValidationClient.checkVotingAbility(ABLE_CPF)).thenReturn(new VotingAbilityResponse(VotingAbilityStatus.ABLE_TO_VOTE));
			when(votingSessionService.isSessionOpen(AGENDA_ID)).thenReturn(false);

			assertThatThrownBy(() -> voteService.registerVote(AGENDA_ID, MEMBER_ID, ABLE_CPF, VoteOption.YES))
					.isInstanceOf(VotingSessionClosedException.class);
			verify(voteRepository, never()).save(any());
		}

		@Test
		@DisplayName("should throw DuplicateVoteException when the member already voted (fast path, RN01)")
		void shouldThrowWhenMemberAlreadyVoted() {
			when(agendaRepository.findById(AGENDA_ID)).thenReturn(Optional.of(new Agenda(AGENDA_ID, "Reforma do estatuto", null)));
			when(cpfValidationClient.checkVotingAbility(ABLE_CPF)).thenReturn(new VotingAbilityResponse(VotingAbilityStatus.ABLE_TO_VOTE));
			when(votingSessionService.isSessionOpen(AGENDA_ID)).thenReturn(true);
			when(voteRepository.existsByAgendaIdAndMemberId(AGENDA_ID, MEMBER_ID)).thenReturn(true);

			assertThatThrownBy(() -> voteService.registerVote(AGENDA_ID, MEMBER_ID, ABLE_CPF, VoteOption.YES))
					.isInstanceOf(DuplicateVoteException.class);
			verify(voteRepository, never()).save(any());
		}

		@Test
		@DisplayName("should throw DuplicateVoteException when the unique constraint rejects a concurrent duplicate (RN01)")
		void shouldThrowWhenDatabaseConstraintIsViolated() {
			when(agendaRepository.findById(AGENDA_ID)).thenReturn(Optional.of(new Agenda(AGENDA_ID, "Reforma do estatuto", null)));
			when(cpfValidationClient.checkVotingAbility(ABLE_CPF)).thenReturn(new VotingAbilityResponse(VotingAbilityStatus.ABLE_TO_VOTE));
			when(votingSessionService.isSessionOpen(AGENDA_ID)).thenReturn(true);
			when(voteRepository.existsByAgendaIdAndMemberId(AGENDA_ID, MEMBER_ID)).thenReturn(false);
			when(voteRepository.save(any(Vote.class))).thenThrow(new DataIntegrityViolationException("uk_votes_agenda_member"));

			assertThatThrownBy(() -> voteService.registerVote(AGENDA_ID, MEMBER_ID, ABLE_CPF, VoteOption.YES))
					.isInstanceOf(DuplicateVoteException.class);
		}
	}
}
