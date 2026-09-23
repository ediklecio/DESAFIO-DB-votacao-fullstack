package com.db.vote.domain.exception;

public class VotingSessionClosedException extends RuntimeException {

	public VotingSessionClosedException(String message) {
		super(message);
	}
}
