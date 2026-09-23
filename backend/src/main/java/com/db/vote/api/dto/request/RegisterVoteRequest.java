package com.db.vote.api.dto.request;

import com.db.vote.domain.VoteOption;

public record RegisterVoteRequest(Long memberId, String cpf, VoteOption voteAnswer) {
}
