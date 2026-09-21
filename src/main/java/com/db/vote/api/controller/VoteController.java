package com.db.vote.api.controller;

import com.db.vote.api.dto.request.RegisterVoteRequest;
import com.db.vote.api.dto.response.VoteResponse;
import com.db.vote.domain.Vote;
import com.db.vote.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agendas/{agendaId}/votes")
@RequiredArgsConstructor
public class VoteController {

	private final VoteService voteService;

	@PostMapping
	public ResponseEntity<VoteResponse> vote(@PathVariable Long agendaId, @RequestBody RegisterVoteRequest request) {
		Vote vote = voteService.registerVote(agendaId, request.memberId(), request.voteAnswer());
		return ResponseEntity.status(HttpStatus.CREATED).body(VoteResponse.from(vote));
	}
}
