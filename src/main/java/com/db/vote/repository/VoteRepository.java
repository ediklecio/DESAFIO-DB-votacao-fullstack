package com.db.vote.repository;

import com.db.vote.domain.Vote;
import com.db.vote.domain.VoteOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {

	boolean existsByAgendaIdAndMemberId(Long agendaId, Long memberId);

	long countByAgendaIdAndVoteAnswer(Long agendaId, VoteOption voteAnswer);
}
