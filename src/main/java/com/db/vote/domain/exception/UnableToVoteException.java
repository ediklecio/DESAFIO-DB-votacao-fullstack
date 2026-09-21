package com.db.vote.domain.exception;

public class UnableToVoteException extends RuntimeException {

	public UnableToVoteException(String message) {
		super(message);
	}
}
