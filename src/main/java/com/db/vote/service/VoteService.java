package com.db.vote.service;

import com.db.vote.domain.Vote;
import com.db.vote.domain.VoteOption;
import com.db.vote.domain.exception.AgendaNotFoundException;
import com.db.vote.domain.exception.DuplicateVoteException;
import com.db.vote.domain.exception.VotingSessionClosedException;
import com.db.vote.repository.AgendaRepository;
import com.db.vote.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VoteService {

	private static final Logger log = LoggerFactory.getLogger(VoteService.class);

	private final VoteRepository voteRepository;
	private final VotingSessionService votingSessionService;
	private final AgendaRepository agendaRepository;

	public Vote registerVote(Long agendaId, Long memberId, VoteOption voteAnswer) {
		agendaRepository.findById(agendaId)
				.orElseThrow(() -> new AgendaNotFoundException("Agenda " + agendaId + " not found"));

		if (!votingSessionService.isSessionOpen(agendaId)) {
			throw new VotingSessionClosedException("Voting session for agenda " + agendaId + " is not open");
		}
		if (voteRepository.existsByAgendaIdAndMemberId(agendaId, memberId)) {
			throw new DuplicateVoteException("Member " + memberId + " already voted on agenda " + agendaId);
		}

		Vote vote = new Vote(null, agendaId, memberId, voteAnswer, LocalDateTime.now());
		try {
			Vote savedVote = voteRepository.save(vote);
			log.info("Vote registered: agendaId={}, memberId={}, answer={}", agendaId, memberId, voteAnswer);
			return savedVote;
		} catch (DataIntegrityViolationException exception) {
			// RN01's real guarantee: the existsBy check above is a fast path only and
			// cannot prevent a race between two concurrent votes from the same member.
			throw new DuplicateVoteException("Member " + memberId + " already voted on agenda " + agendaId);
		}
	}
}
