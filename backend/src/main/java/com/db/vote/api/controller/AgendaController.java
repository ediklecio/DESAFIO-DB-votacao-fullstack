package com.db.vote.api.controller;

import com.db.vote.api.dto.request.CreateAgendaRequest;
import com.db.vote.api.dto.response.AgendaOverviewResponse;
import com.db.vote.api.dto.response.AgendaResponse;
import com.db.vote.api.dto.response.PageResponse;
import com.db.vote.domain.Agenda;
import com.db.vote.service.AgendaQueryService;
import com.db.vote.service.AgendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
	private final AgendaQueryService agendaQueryService;

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

	@GetMapping
	@Operation(summary = "Lista pautas (paginado) com status da sessão e contagem de votos",
			description = "Mais recentes primeiro. Parâmetros: page (0-based), size, sort.")
	public ResponseEntity<PageResponse<AgendaOverviewResponse>> list(
			@PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
		return ResponseEntity.ok(PageResponse.from(agendaQueryService.listAgendas(pageable)));
	}

	@GetMapping("/{agendaId}")
	@Operation(summary = "Detalha uma pauta com status da sessão, tempo restante e contagem de votos")
	public ResponseEntity<AgendaOverviewResponse> get(@PathVariable Long agendaId) {
		return ResponseEntity.ok(agendaQueryService.getAgenda(agendaId));
	}
}
