package com.db.vote.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "voting_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

	// Hand-written, not @AllArgsConstructor: RN03's default-duration fallback
	// is behavior Lombok's generated constructors cannot express.
	public VotingSession(Long agendaId, LocalDateTime openedAt, Integer durationMinutes) {
		this.agendaId = agendaId;
		this.openedAt = openedAt;
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
}
