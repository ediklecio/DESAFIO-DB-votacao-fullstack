package com.db.vote.repository;

import com.db.vote.domain.Vote;
import com.db.vote.domain.VoteOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Long> {

	boolean existsByAgendaIdAndMemberId(Long agendaId, Long memberId);

	boolean existsByAgendaIdAndCpf(Long agendaId, String cpf);

	long countByAgendaIdAndVoteAnswer(Long agendaId, VoteOption voteAnswer);

	// One grouped query for a whole page of agendas - avoids an N+1 of two
	// count queries per agenda when listing.
	@Query("""
			select v.agendaId as agendaId, v.voteAnswer as voteAnswer, count(v) as total
			from Vote v
			where v.agendaId in :agendaIds
			group by v.agendaId, v.voteAnswer
			""")
	List<VoteCount> countGroupedByAgendaIdAndVoteAnswer(@Param("agendaIds") Collection<Long> agendaIds);

	interface VoteCount {
		Long getAgendaId();

		VoteOption getVoteAnswer();

		Long getTotal();
	}
}
