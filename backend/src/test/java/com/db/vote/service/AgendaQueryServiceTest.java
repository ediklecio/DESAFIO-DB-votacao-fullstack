package com.db.vote.service;

import com.db.vote.api.dto.response.AgendaOverviewResponse;
import com.db.vote.domain.Agenda;
import com.db.vote.domain.SessionStatus;
import com.db.vote.domain.VoteOption;
import com.db.vote.domain.VotingSession;
import com.db.vote.domain.exception.AgendaNotFoundException;
import com.db.vote.repository.AgendaRepository;
import com.db.vote.repository.VoteRepository;
import com.db.vote.repository.VoteRepository.VoteCount;
import com.db.vote.repository.VotingSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendaQueryServiceTest {

	@Mock
	private AgendaRepository agendaRepository;

	@Mock
	private VotingSessionRepository votingSessionRepository;

	@Mock
	private VoteRepository voteRepository;

	private AgendaQueryService agendaQueryService;

	@BeforeEach
	void setUp() {
		agendaQueryService = new AgendaQueryService(agendaRepository, votingSessionRepository, voteRepository);
	}

	@Test
	void shouldDeriveSessionStatusAndTallyForEachAgendaOnThePage() {
		Pageable pageable = PageRequest.of(0, 20);
		Agenda notStarted = new Agenda(1L, "Reforma do estatuto", null);
		Agenda open = new Agenda(2L, "Eleição do conselho", null);
		Agenda closed = new Agenda(3L, "Destinação das sobras", null);
		when(agendaRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(notStarted, open, closed), pageable, 3));
		when(votingSessionRepository.findByAgendaIdIn(anyCollection())).thenReturn(List.of(
				new VotingSession(2L, LocalDateTime.now(), 10),
				new VotingSession(3L, LocalDateTime.now().minusMinutes(5), 1)));
		when(voteRepository.countGroupedByAgendaIdAndVoteAnswer(anyCollection())).thenReturn(List.of(
				voteCount(2L, VoteOption.YES, 4L),
				voteCount(3L, VoteOption.YES, 1L),
				voteCount(3L, VoteOption.NO, 2L)));

		Page<AgendaOverviewResponse> page = agendaQueryService.listAgendas(pageable);

		assertThat(page.getTotalElements()).isEqualTo(3);
		assertThat(page.getContent()).extracting(AgendaOverviewResponse::sessionStatus)
				.containsExactly(SessionStatus.NOT_STARTED, SessionStatus.OPEN, SessionStatus.CLOSED);

		AgendaOverviewResponse first = page.getContent().get(0);
		assertThat(first.totalYes()).isZero();
		assertThat(first.openedAt()).isNull();

		AgendaOverviewResponse second = page.getContent().get(1);
		assertThat(second.totalYes()).isEqualTo(4L);
		assertThat(second.secondsRemaining()).isPositive();

		AgendaOverviewResponse third = page.getContent().get(2);
		assertThat(third.totalYes()).isEqualTo(1L);
		assertThat(third.totalNo()).isEqualTo(2L);
		assertThat(third.secondsRemaining()).isZero();
	}

	@Test
	void shouldNotQuerySessionsOrVotesWhenThePageIsEmpty() {
		Pageable pageable = PageRequest.of(0, 20);
		when(agendaRepository.findAll(pageable)).thenReturn(Page.empty(pageable));

		Page<AgendaOverviewResponse> page = agendaQueryService.listAgendas(pageable);

		assertThat(page.getContent()).isEmpty();
		verifyNoInteractions(votingSessionRepository, voteRepository);
	}

	@Test
	void shouldReturnASingleAgendaOverview() {
		when(agendaRepository.findById(1L)).thenReturn(Optional.of(new Agenda(1L, "Reforma do estatuto", "desc")));
		when(votingSessionRepository.findByAgendaIdIn(anyCollection())).thenReturn(List.of());
		when(voteRepository.countGroupedByAgendaIdAndVoteAnswer(anyCollection())).thenReturn(List.of());

		AgendaOverviewResponse overview = agendaQueryService.getAgenda(1L);

		assertThat(overview.id()).isEqualTo(1L);
		assertThat(overview.description()).isEqualTo("desc");
		assertThat(overview.sessionStatus()).isEqualTo(SessionStatus.NOT_STARTED);
	}

	@Test
	void shouldThrowAgendaNotFoundExceptionWhenAgendaDoesNotExist() {
		when(agendaRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> agendaQueryService.getAgenda(99L))
				.isInstanceOf(AgendaNotFoundException.class);
	}

	private static VoteCount voteCount(Long agendaId, VoteOption answer, Long total) {
		return new VoteCount() {
			@Override
			public Long getAgendaId() {
				return agendaId;
			}

			@Override
			public VoteOption getVoteAnswer() {
				return answer;
			}

			@Override
			public Long getTotal() {
				return total;
			}
		};
	}
}
