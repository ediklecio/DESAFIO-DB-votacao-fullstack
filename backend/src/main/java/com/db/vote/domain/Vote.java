package com.db.vote.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
		name = "votes",
		// RN01: a member votes at most once per agenda, and neither the member_id
		// nor the cpf may be reused with a different counterpart for the same
		// agenda. Enforced here at the database level — a service-side "exists"
		// check alone would still allow a duplicate under concurrent requests.
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_votes_agenda_member", columnNames = {"agenda_id", "member_id"}),
				@UniqueConstraint(name = "uk_votes_agenda_cpf", columnNames = {"agenda_id", "cpf"})
		}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Vote {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "agenda_id", nullable = false)
	private Long agendaId;

	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@Column(name = "cpf", nullable = false, length = 11)
	private String cpf;

	@Enumerated(EnumType.STRING)
	@Column(name = "vote_answer", nullable = false)
	private VoteOption voteAnswer;

	@Column(name = "registered_at", nullable = false)
	private LocalDateTime registeredAt;
}
