package com.db.vote.infra.client;

import com.db.vote.domain.exception.InvalidCpfException;
import org.springframework.stereotype.Component;

@Component
public class FakeCpfValidationClientAdapter implements CpfValidationClient {

	private static final String ELEVEN_DIGITS = "\\d{11}";

	@Override
	public VotingAbilityResponse checkVotingAbility(String cpf) {
		if (cpf == null || !cpf.matches(ELEVEN_DIGITS)) {
			throw new InvalidCpfException("CPF " + cpf + " is not a valid CPF");
		}

		int lastDigit = cpf.charAt(cpf.length() - 1) - '0';
		VotingAbilityStatus status = lastDigit % 2 == 0 ? VotingAbilityStatus.ABLE_TO_VOTE : VotingAbilityStatus.UNABLE_TO_VOTE;
		return new VotingAbilityResponse(status);
	}
}
