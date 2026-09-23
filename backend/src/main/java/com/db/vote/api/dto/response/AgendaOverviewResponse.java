package com.db.vote.api.dto.response;

import com.db.vote.domain.SessionStatus;

import java.time.LocalDateTime;

/**
 * Read model of an agenda with its session state and partial/final tally.
 * secondsRemaining is computed server-side so clients can run a countdown
 * without depending on their own clock or timezone matching the server's.
 */
public record AgendaOverviewResponse(
		Long id,
		String title,
		String description,
		SessionStatus sessionStatus,
		LocalDateTime openedAt,
		LocalDateTime closesAt,
		long secondsRemaining,
		long totalYes,
		long totalNo) {
}
