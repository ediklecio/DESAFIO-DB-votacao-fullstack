package com.db.vote.domain.exception;

/**
 * Thrown when a member tries to vote more than once on the same agenda (RN01).
 * The service layer raises this after catching the database-level unique
 * constraint violation on {@code (agenda_id, member_id)} — see {@code Vote}.
 */
public class DuplicateVoteException extends RuntimeException {

	public DuplicateVoteException(String message) {
		super(message);
	}
}
