package com.db.vote.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.time.LocalDateTime;

@Entity
@Table(name = "voting_sessions")
public class VotingSession {

	private static final int DEFAULT_DURATION_MINUTES = 1;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "agenda_id", nullable = false)
	private Long agendaId;

	@Column(name = "opened_at", nullable = false)
	private LocalDateTime openedAt;

	@Column(name = "duration_minutes", nullable = false)
	private Integer durationMinutes;

	protected VotingSession() {
	}

	public VotingSession(Long agendaId, LocalDateTime openedAt, Integer durationMinutes) {
		this.agendaId = agendaId;
		this.openedAt = openedAt;
		// RN03: default duration is 1 minute when none is informed at opening.
		this.durationMinutes = durationMinutes != null ? durationMinutes : DEFAULT_DURATION_MINUTES;
	}

	// RN05: open/closed status is derived from the current time, never stored or
	// driven by a scheduled job, so closesAt is computed rather than persisted.
	@Transient
	public LocalDateTime getClosesAt() {
		return openedAt.plusMinutes(durationMinutes);
	}

	public boolean isOpen(LocalDateTime now) {
		return !now.isBefore(openedAt) && now.isBefore(getClosesAt());
	}

	public Long getId() {
		return id;
	}

	public Long getAgendaId() {
		return agendaId;
	}

	public LocalDateTime getOpenedAt() {
		return openedAt;
	}

	public Integer getDurationMinutes() {
		return durationMinutes;
	}
}
