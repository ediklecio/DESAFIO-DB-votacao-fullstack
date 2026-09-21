package com.db.vote.infra.client;

import com.db.vote.domain.exception.InvalidCpfException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * This is a FAKE, in-process adapter (no real external service exists) - the
 * class diagram (docs/02-diagrama-classes.md) shows no HTTP dependency on it.
 * The rule below (last digit parity) is an arbitrary but documented stand-in
 * for a real CPF authority, kept deterministic so it is testable.
 */
class FakeCpfValidationClientAdapterTest {

	private final FakeCpfValidationClientAdapter adapter = new FakeCpfValidationClientAdapter();

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {"123", "1234567890", "abcdefghijk", "123456789012"})
	void shouldThrowInvalidCpfExceptionForAnythingOtherThanElevenDigits(String cpf) {
		assertThatThrownBy(() -> adapter.checkVotingAbility(cpf))
				.isInstanceOf(InvalidCpfException.class);
	}

	@Test
	void shouldReturnAbleToVoteWhenTheLastDigitIsEven() {
		VotingAbilityResponse response = adapter.checkVotingAbility("11111111110");

		assertThat(response.status()).isEqualTo(VotingAbilityStatus.ABLE_TO_VOTE);
	}

	@Test
	void shouldReturnUnableToVoteWhenTheLastDigitIsOdd() {
		VotingAbilityResponse response = adapter.checkVotingAbility("11111111111");

		assertThat(response.status()).isEqualTo(VotingAbilityStatus.UNABLE_TO_VOTE);
	}
}
