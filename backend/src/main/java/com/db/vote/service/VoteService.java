package com.db.vote.service;

import com.db.vote.domain.Vote;
import com.db.vote.domain.VoteOption;
import com.db.vote.domain.exception.AgendaNotFoundException;
import com.db.vote.domain.exception.DuplicateVoteException;
import com.db.vote.domain.exception.UnableToVoteException;
import com.db.vote.domain.exception.VotingSessionClosedException;
import com.db.vote.infra.client.CpfValidationClient;
import com.db.vote.infra.client.VotingAbilityStatus;
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
	private final CpfValidationClient cpfValidationClient;

	public Vote registerVote(Long agendaId, Long memberId, String cpf, VoteOption voteAnswer) {
		agendaRepository.findById(agendaId)
				.orElseThrow(() -> new AgendaNotFoundException("Agenda " + agendaId + " not found"));

		// RF06/RN07: an invalid CPF propagates as InvalidCpfException (404) straight
		// from the client; UNABLE_TO_VOTE must not be silently discarded.
		if (cpfValidationClient.checkVotingAbility(cpf).status() == VotingAbilityStatus.UNABLE_TO_VOTE) {
			throw new UnableToVoteException("Member " + memberId + " is not able to vote");
		}

		if (!votingSessionService.isSessionOpen(agendaId)) {
			throw new VotingSessionClosedException("Voting session for agenda " + agendaId + " is not open");
		}
		if (voteRepository.existsByAgendaIdAndMemberId(agendaId, memberId)) {
			throw new DuplicateVoteException("Member " + memberId + " already voted on agenda " + agendaId);
		}
		// RN01 extended: a CPF already used on this agenda cannot vote again
		// under a different member_id, and vice-versa (checked above).
		if (voteRepository.existsByAgendaIdAndCpf(agendaId, cpf)) {
			throw new DuplicateVoteException("CPF " + cpf + " already voted on agenda " + agendaId);
		}

		Vote vote = new Vote(null, agendaId, memberId, cpf, voteAnswer, LocalDateTime.now());
		try {
			Vote savedVote = voteRepository.save(vote);
			log.info("Vote registered: agendaId={}, memberId={}, answer={}", agendaId, memberId, voteAnswer);
			return savedVote;
		} catch (DataIntegrityViolationException exception) {
			// RN01's real guarantee: the existsBy checks above are a fast path only
			// and cannot prevent a race between two concurrent votes from the same
			// member or CPF.
			throw new DuplicateVoteException("Member " + memberId + " or CPF " + cpf + " already voted on agenda " + agendaId);
		}
	}
}
