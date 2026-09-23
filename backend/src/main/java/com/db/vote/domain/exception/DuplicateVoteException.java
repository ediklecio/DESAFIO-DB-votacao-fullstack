package com.db.vote.domain.exception;

/**
 * Thrown when a member or CPF tries to vote more than once on the same
 * agenda (RN01). The service layer raises this after catching the
 * database-level unique constraint violation on {@code (agenda_id, member_id)}
 * or {@code (agenda_id, cpf)} — see {@code Vote}.
 */
public class DuplicateVoteException extends RuntimeException {

	public DuplicateVoteException(String message) {
		super(message);
	}
}
