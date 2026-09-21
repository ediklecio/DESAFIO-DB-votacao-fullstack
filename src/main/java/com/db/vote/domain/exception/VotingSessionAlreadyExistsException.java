package com.db.vote.domain.exception;

/**
 * An agenda has at most one voting session (docs/02-diagrama-classes.md models
 * Agenda "1" --> "0..1" VotingSession) - thrown when trying to open a second one.
 */
public class VotingSessionAlreadyExistsException extends RuntimeException {

	public VotingSessionAlreadyExistsException(String message) {
		super(message);
	}
}
