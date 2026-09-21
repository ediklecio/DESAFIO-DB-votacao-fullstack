package com.db.vote.api.dto.request;

import com.db.vote.domain.VoteOption;

// No cpf field yet: RF06 (CPF validation) is a separate bonus backlog item that
// will extend this DTO when it is tackled - adding it now unused would be dead weight.
public record RegisterVoteRequest(Long memberId, VoteOption voteAnswer) {
}
