package com.db.vote.service;

import com.db.vote.api.dto.response.AgendaOverviewResponse;
import com.db.vote.domain.Agenda;
import com.db.vote.domain.SessionStatus;
import com.db.vote.domain.VoteOption;
import com.db.vote.domain.VotingSession;
import com.db.vote.domain.exception.AgendaNotFoundException;
import com.db.vote.repository.AgendaRepository;
import com.db.vote.repository.VoteRepository;
import com.db.vote.repository.VoteRepository.VoteCount;
import com.db.vote.repository.VotingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Read side of agendas: combines each agenda with its session state and vote
 * tally. Kept apart from AgendaService (write side) so each has one reason to change.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AgendaQueryService {

	private final AgendaRepository agendaRepository;
	private final VotingSessionRepository votingSessionRepository;
	private final VoteRepository voteRepository;

	public Page<AgendaOverviewResponse> listAgendas(Pageable pageable) {
		Page<Agenda> agendas = agendaRepository.findAll(pageable);
		List<AgendaOverviewResponse> overviews = agendas.isEmpty() ? List.of() : toOverviews(agendas.getContent());
		return new PageImpl<>(overviews, agendas.getPageable(), agendas.getTotalElements());
	}

	public AgendaOverviewResponse getAgenda(Long agendaId) {
		Agenda agenda = agendaRepository.findById(agendaId)
				.orElseThrow(() -> new AgendaNotFoundException("Agenda " + agendaId + " not found"));
		return toOverviews(List.of(agenda)).getFirst();
	}

	// Two batched queries (sessions + grouped vote counts) regardless of how many
	// agendas are on the page - no N+1.
	private List<AgendaOverviewResponse> toOverviews(List<Agenda> agendas) {
		List<Long> agendaIds = agendas.stream().map(Agenda::getId).toList();
		Map<Long, VotingSession> sessionsByAgendaId = votingSessionRepository.findByAgendaIdIn(agendaIds).stream()
				.collect(Collectors.toMap(VotingSession::getAgendaId, Function.identity()));
		Map<Long, Map<VoteOption, Long>> countsByAgendaId = countVotesByAgenda(agendaIds);
		LocalDateTime now = LocalDateTime.now();

		return agendas.stream()
				.map(agenda -> toOverview(agenda, sessionsByAgendaId.get(agenda.getId()),
						countsByAgendaId.getOrDefault(agenda.getId(), Map.of()), now))
				.toList();
	}

	private Map<Long, Map<VoteOption, Long>> countVotesByAgenda(List<Long> agendaIds) {
		Map<Long, Map<VoteOption, Long>> counts = new HashMap<>();
		for (VoteCount voteCount : voteRepository.countGroupedByAgendaIdAndVoteAnswer(agendaIds)) {
			counts.computeIfAbsent(voteCount.getAgendaId(), id -> new EnumMap<>(VoteOption.class))
					.put(voteCount.getVoteAnswer(), voteCount.getTotal());
		}
		return counts;
	}

	private AgendaOverviewResponse toOverview(Agenda agenda, VotingSession session,
			Map<VoteOption, Long> counts, LocalDateTime now) {
		long totalYes = counts.getOrDefault(VoteOption.YES, 0L);
		long totalNo = counts.getOrDefault(VoteOption.NO, 0L);
		if (session == null) {
			return new AgendaOverviewResponse(agenda.getId(), agenda.getTitle(), agenda.getDescription(),
					SessionStatus.NOT_STARTED, null, null, 0, totalYes, totalNo);
		}
		SessionStatus status = session.statusAt(now);
		long secondsRemaining = status == SessionStatus.OPEN
				? Duration.between(now, session.getClosesAt()).toSeconds()
				: 0;
		return new AgendaOverviewResponse(agenda.getId(), agenda.getTitle(), agenda.getDescription(),
				status, session.getOpenedAt(), session.getClosesAt(), secondsRemaining, totalYes, totalNo);
	}
}
