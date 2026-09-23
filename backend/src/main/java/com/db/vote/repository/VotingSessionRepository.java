package com.db.vote.repository;

import com.db.vote.domain.VotingSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface VotingSessionRepository extends JpaRepository<VotingSession, Long> {

	Optional<VotingSession> findByAgendaId(Long agendaId);

	List<VotingSession> findByAgendaIdIn(Collection<Long> agendaIds);
}
