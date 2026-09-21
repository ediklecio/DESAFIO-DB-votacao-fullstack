package com.db.vote.api.controller;

import com.db.vote.api.dto.request.RegisterVoteRequest;
import com.db.vote.api.dto.response.VoteResponse;
import com.db.vote.api.dto.response.VotingResultResponse;
import com.db.vote.domain.Vote;
import com.db.vote.service.VoteService;
import com.db.vote.service.VotingResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// Paths declared per method (no class-level @RequestMapping): /votes and
// /results are siblings under the same agenda, not one nested under the other.
@RestController
@RequiredArgsConstructor
public class VoteController {

	private final VoteService voteService;
	private final VotingResultService votingResultService;

	@PostMapping("/api/v1/agendas/{agendaId}/votes")
	public ResponseEntity<VoteResponse> vote(@PathVariable Long agendaId, @RequestBody RegisterVoteRequest request) {
		Vote vote = voteService.registerVote(agendaId, request.memberId(), request.cpf(), request.voteAnswer());
		return ResponseEntity.status(HttpStatus.CREATED).body(VoteResponse.from(vote));
	}

	@GetMapping("/api/v1/agendas/{agendaId}/results")
	public ResponseEntity<VotingResultResponse> result(@PathVariable Long agendaId) {
		return ResponseEntity.ok(votingResultService.getResult(agendaId));
	}
}
