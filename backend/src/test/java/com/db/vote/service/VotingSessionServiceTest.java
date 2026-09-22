package com.db.vote.service;

import com.db.vote.domain.Agenda;
import com.db.vote.domain.VotingSession;
import com.db.vote.domain.exception.AgendaNotFoundException;
import com.db.vote.domain.exception.VotingSessionAlreadyExistsException;
import com.db.vote.repository.AgendaRepository;
import com.db.vote.repository.VotingSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VotingSessionServiceTest {

	private static final Long AGENDA_ID = 1L;

	@Mock
	private VotingSessionRepository votingSessionRepository;

	@Mock
	private AgendaRepository agendaRepository;

	private VotingSessionService votingSessionService;

	@BeforeEach
	void setUp() {
		votingSessionService = new VotingSessionService(votingSessionRepository, agendaRepository);
	}

	@Nested
	@DisplayName("When opening a session")
	class WhenOpeningASession {

		@Test
		@DisplayName("should persist a session with the given duration")
		void shouldOpenSessionWithGivenDuration() {
			when(agendaRepository.findById(AGENDA_ID)).thenReturn(Optional.of(new Agenda(AGENDA_ID, "Reforma do estatuto", null)));
			when(votingSessionRepository.findByAgendaId(AGENDA_ID)).thenReturn(Optional.empty());
			when(votingSessionRepository.save(any(VotingSession.class)))
					.thenAnswer(invocation -> invocation.getArgument(0));

			VotingSession result = votingSessionService.openSession(AGENDA_ID, 5);

			ArgumentCaptor<VotingSession> captor = ArgumentCaptor.forClass(VotingSession.class);
			verify(votingSessionRepository).save(captor.capture());
			assertThat(captor.getValue().getAgendaId()).isEqualTo(AGENDA_ID);
			assertThat(captor.getValue().getDurationMinutes()).isEqualTo(5);
			assertThat(result.getDurationMinutes()).isEqualTo(5);
		}

		@Test
		@DisplayName("should default the duration to 1 minute when not informed (RN03)")
		void shouldOpenSessionWithDefaultDurationWhenNotInformed() {
			when(agendaRepository.findById(AGENDA_ID)).thenReturn(Optional.of(new Agenda(AGENDA_ID, "Reforma do estatuto", null)));
			when(votingSessionRepository.findByAgendaId(AGENDA_ID)).thenReturn(Optional.empty());
			when(votingSessionRepository.save(any(VotingSession.class)))
					.thenAnswer(invocation -> invocation.getArgument(0));

			VotingSession result = votingSessionService.openSession(AGENDA_ID, null);

			assertThat(result.getDurationMinutes()).isEqualTo(1);
		}

		@Test
		@DisplayName("should throw AgendaNotFoundException when the agenda does not exist")
		void shouldThrowWhenAgendaDoesNotExist() {
			when(agendaRepository.findById(AGENDA_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> votingSessionService.openSession(AGENDA_ID, null))
					.isInstanceOf(AgendaNotFoundException.class);
			verify(votingSessionRepository, never()).save(any());
		}

		@Test
		@DisplayName("should throw VotingSessionAlreadyExistsException when the agenda already has a session")
		void shouldThrowWhenAgendaAlreadyHasASession() {
			when(agendaRepository.findById(AGENDA_ID)).thenReturn(Optional.of(new Agenda(AGENDA_ID, "Reforma do estatuto", null)));
			var existingSession = new VotingSession(AGENDA_ID, LocalDateTime.now(), 1);
			when(votingSessionRepository.findByAgendaId(AGENDA_ID)).thenReturn(Optional.of(existingSession));

			assertThatThrownBy(() -> votingSessionService.openSession(AGENDA_ID, null))
					.isInstanceOf(VotingSessionAlreadyExistsException.class);
			verify(votingSessionRepository, never()).save(any());
		}
	}

	@Nested
	@DisplayName("When checking whether a session is open")
	class WhenCheckingIfSessionIsOpen {

		@Test
		@DisplayName("should return false when the agenda has no session at all (RN04)")
		void shouldReturnFalseWhenNoSessionExists() {
			when(votingSessionRepository.findByAgendaId(AGENDA_ID)).thenReturn(Optional.empty());

			assertThat(votingSessionService.isSessionOpen(AGENDA_ID)).isFalse();
		}

		@Test
		@DisplayName("should return true while within the session's time window")
		void shouldReturnTrueWithinTheWindow() {
			var session = new VotingSession(AGENDA_ID, LocalDateTime.now().minusSeconds(30), 1);
			when(votingSessionRepository.findByAgendaId(AGENDA_ID)).thenReturn(Optional.of(session));

			assertThat(votingSessionService.isSessionOpen(AGENDA_ID)).isTrue();
		}

		@Test
		@DisplayName("should return false once the session's window has elapsed")
		void shouldReturnFalseAfterTheWindowElapses() {
			var session = new VotingSession(AGENDA_ID, LocalDateTime.now().minusMinutes(5), 1);
			when(votingSessionRepository.findByAgendaId(AGENDA_ID)).thenReturn(Optional.of(session));

			assertThat(votingSessionService.isSessionOpen(AGENDA_ID)).isFalse();
		}
	}
}
