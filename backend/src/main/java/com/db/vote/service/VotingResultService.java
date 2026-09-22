package com.db.vote.service;

import com.db.vote.api.dto.response.VotingResultResponse;
import com.db.vote.domain.VoteOption;
import com.db.vote.domain.exception.AgendaNotFoundException;
import com.db.vote.repository.AgendaRepository;
import com.db.vote.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VotingResultService {

	private final VoteRepository voteRepository;
	private final VotingSessionService votingSessionService;
	private final AgendaRepository agendaRepository;

	public VotingResultResponse getResult(Long agendaId) {
		agendaRepository.findById(agendaId)
				.orElseThrow(() -> new AgendaNotFoundException("Agenda " + agendaId + " not found"));

		long totalYes = voteRepository.countByAgendaIdAndVoteAnswer(agendaId, VoteOption.YES);
		long totalNo = voteRepository.countByAgendaIdAndVoteAnswer(agendaId, VoteOption.NO);
		// RN06: the response must make clear whether the session is closed or the
		// count is still partial.
		boolean sessionClosed = !votingSessionService.isSessionOpen(agendaId);
		return new VotingResultResponse(agendaId, totalYes, totalNo, sessionClosed);
	}
}
