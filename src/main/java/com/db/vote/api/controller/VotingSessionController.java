package com.db.vote.api.controller;

import com.db.vote.api.dto.request.OpenVotingSessionRequest;
import com.db.vote.api.dto.response.VotingSessionResponse;
import com.db.vote.domain.VotingSession;
import com.db.vote.service.VotingSessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agendas/{agendaId}/sessions")
@RequiredArgsConstructor
@Tag(name = "Sessão", description = "Abertura de sessões de votação")
public class VotingSessionController {

	private final VotingSessionService votingSessionService;

	@PostMapping
	public ResponseEntity<VotingSessionResponse> open(
			@PathVariable Long agendaId,
			@RequestBody(required = false) OpenVotingSessionRequest request) {
		Integer durationMinutes = request != null ? request.durationMinutes() : null;
		VotingSession session = votingSessionService.openSession(agendaId, durationMinutes);
		return ResponseEntity.status(HttpStatus.CREATED).body(VotingSessionResponse.from(session));
	}
}
