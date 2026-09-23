package com.db.vote.service;

import com.db.vote.domain.VotingSession;
import com.db.vote.domain.exception.AgendaNotFoundException;
import com.db.vote.domain.exception.VotingSessionAlreadyExistsException;
import com.db.vote.repository.AgendaRepository;
import com.db.vote.repository.VotingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VotingSessionService {

	private static final Logger log = LoggerFactory.getLogger(VotingSessionService.class);

	private final VotingSessionRepository votingSessionRepository;
	private final AgendaRepository agendaRepository;

	public VotingSession openSession(Long agendaId, Integer durationMinutes) {
		agendaRepository.findById(agendaId)
				.orElseThrow(() -> new AgendaNotFoundException("Agenda " + agendaId + " not found"));
		if (votingSessionRepository.findByAgendaId(agendaId).isPresent()) {
			throw new VotingSessionAlreadyExistsException("Agenda " + agendaId + " already has a voting session");
		}

		VotingSession session = new VotingSession(agendaId, LocalDateTime.now(), durationMinutes);
		VotingSession savedSession = votingSessionRepository.save(session);
		log.info("Voting session opened: agendaId={}, closesAt={}", agendaId, savedSession.getClosesAt());
		return savedSession;
	}

	public boolean isSessionOpen(Long agendaId) {
		return votingSessionRepository.findByAgendaId(agendaId)
				.map(session -> session.isOpen(LocalDateTime.now()))
				.orElse(false);
	}
}
