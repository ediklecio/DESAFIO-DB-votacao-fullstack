package com.db.vote.service;

import com.db.vote.api.dto.request.CreateAgendaRequest;
import com.db.vote.domain.Agenda;
import com.db.vote.domain.exception.InvalidAgendaException;
import com.db.vote.repository.AgendaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AgendaService#createAgenda(CreateAgendaRequest)} (RF01 - Cadastrar pauta).
 * Written TDD-first: {@code AgendaRepository}, {@code AgendaService} and
 * {@code CreateAgendaRequest} do not exist yet.
 */
@ExtendWith(MockitoExtension.class)
class AgendaServiceTest {

	@Mock
	private AgendaRepository agendaRepository;

	private AgendaService agendaService;

	@Nested
	@DisplayName("When the request is valid")
	class WhenRequestIsValid {

		@Test
		@DisplayName("should persist an agenda with the given title and description")
		void shouldPersistAgendaWithTitleAndDescription() {
			agendaService = new AgendaService(agendaRepository);
			var request = new CreateAgendaRequest("Reforma do estatuto", "Votação sobre a nova redação do estatuto social");
			var persistedAgenda = new Agenda(1L, request.title(), request.description());
			when(agendaRepository.save(any(Agenda.class))).thenReturn(persistedAgenda);

			Agenda result = agendaService.createAgenda(request);

			ArgumentCaptor<Agenda> captor = ArgumentCaptor.forClass(Agenda.class);
			verify(agendaRepository).save(captor.capture());
			Agenda agendaSentToRepository = captor.getValue();
			assertThat(agendaSentToRepository.getTitle()).isEqualTo(request.title());
			assertThat(agendaSentToRepository.getDescription()).isEqualTo(request.description());
			// result must be repository.save()'s return value, not the pre-save transient object,
			// otherwise the generated id would be lost.
			assertThat(result.getId()).isEqualTo(1L);
			assertThat(result.getTitle()).isEqualTo(request.title());
			assertThat(result.getDescription()).isEqualTo(request.description());
		}

		@ParameterizedTest
		@NullAndEmptySource
		@ValueSource(strings = {" ", "\t"})
		@DisplayName("should accept a null, empty or blank description, since only the title is mandatory")
		void shouldAcceptMissingDescription(String description) {
			agendaService = new AgendaService(agendaRepository);
			var request = new CreateAgendaRequest("Eleição da diretoria", description);
			when(agendaRepository.save(any(Agenda.class))).thenReturn(new Agenda(2L, request.title(), description));

			Agenda result = agendaService.createAgenda(request);

			assertThat(result.getId()).isEqualTo(2L);
			assertThat(result.getTitle()).isEqualTo(request.title());
			verify(agendaRepository).save(any(Agenda.class));
		}
	}

	@Nested
	@DisplayName("When the request is invalid")
	class WhenRequestIsInvalid {

		@ParameterizedTest
		@NullAndEmptySource
		@ValueSource(strings = {" ", "\t", "\n"})
		@DisplayName("should reject an agenda with a null, empty or blank title")
		void shouldRejectMissingTitle(String title) {
			agendaService = new AgendaService(agendaRepository);
			var request = new CreateAgendaRequest(title, "Descrição qualquer");

			assertThatThrownBy(() -> agendaService.createAgenda(request))
					.isInstanceOf(InvalidAgendaException.class);
			verify(agendaRepository, never()).save(any());
		}
	}

	@Nested
	@DisplayName("When the repository fails")
	class WhenRepositoryFails {

		@Test
		@DisplayName("should propagate the repository exception instead of swallowing it")
		void shouldPropagateRepositoryException() {
			agendaService = new AgendaService(agendaRepository);
			var request = new CreateAgendaRequest("Reforma do estatuto", "Descrição válida");
			when(agendaRepository.save(any(Agenda.class))).thenThrow(new RuntimeException("database unavailable"));

			assertThatThrownBy(() -> agendaService.createAgenda(request))
					.isInstanceOf(RuntimeException.class)
					.hasMessage("database unavailable");
		}
	}
}
