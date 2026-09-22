package com.db.vote.api.dto.response;

import com.db.vote.domain.Vote;
import com.db.vote.domain.VoteOption;

import java.time.LocalDateTime;

public record VoteResponse(Long id, Long agendaId, Long memberId, VoteOption voteAnswer, LocalDateTime registeredAt) {

	public static VoteResponse from(Vote vote) {
		return new VoteResponse(vote.getId(), vote.getAgendaId(), vote.getMemberId(), vote.getVoteAnswer(), vote.getRegisteredAt());
	}
}
