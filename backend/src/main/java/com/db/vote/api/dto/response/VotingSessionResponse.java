package com.db.vote.api.dto.response;

import com.db.vote.domain.VotingSession;

import java.time.LocalDateTime;

public record VotingSessionResponse(Long id, Long agendaId, LocalDateTime openedAt, LocalDateTime closesAt) {

	public static VotingSessionResponse from(VotingSession session) {
		return new VotingSessionResponse(session.getId(), session.getAgendaId(), session.getOpenedAt(), session.getClosesAt());
	}
}
