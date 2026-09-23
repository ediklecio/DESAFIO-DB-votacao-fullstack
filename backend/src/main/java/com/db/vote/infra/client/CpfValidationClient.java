package com.db.vote.infra.client;

public interface CpfValidationClient {

	VotingAbilityResponse checkVotingAbility(String cpf);
}
