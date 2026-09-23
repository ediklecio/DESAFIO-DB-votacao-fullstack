package com.db.vote.domain;

/**
 * Voting session state as seen by clients. NOT_STARTED means the agenda has no
 * session yet; OPEN/CLOSED are derived from the clock (RN05), never stored.
 */
public enum SessionStatus {
	NOT_STARTED,
	OPEN,
	CLOSED
}
