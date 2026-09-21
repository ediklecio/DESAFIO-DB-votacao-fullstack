package com.db.vote.api.dto.response;

public record VotingResultResponse(Long agendaId, long totalYes, long totalNo, boolean sessionClosed) {
}
