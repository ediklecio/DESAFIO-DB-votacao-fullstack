package com.db.vote.api.controller;

import com.db.vote.api.dto.request.CreateAgendaRequest;
import com.db.vote.api.dto.response.AgendaResponse;
import com.db.vote.domain.Agenda;
import com.db.vote.service.AgendaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/agendas")
@RequiredArgsConstructor
@Tag(name = "Pauta", description = "Criação e consulta de pautas de votação")
public class AgendaController {

	private final AgendaService agendaService;

	// No @Valid here on purpose: AgendaService already validates the request via
	// the same @NotBlank constraint, and is the single enforcement point tested
	// directly (see AgendaServiceTest) - adding @Valid would short-circuit before
	// reaching it, making that validation path dead code for HTTP callers.
	@PostMapping
	public ResponseEntity<AgendaResponse> create(@RequestBody CreateAgendaRequest request) {
		Agenda agenda = agendaService.createAgenda(request);
		return ResponseEntity.created(URI.create("/api/v1/agendas/" + agenda.getId()))
				.body(AgendaResponse.from(agenda));
	}
}
