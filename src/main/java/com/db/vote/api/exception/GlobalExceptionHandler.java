package com.db.vote.api.exception;

import com.db.vote.api.dto.response.ErrorResponse;
import com.db.vote.domain.exception.AgendaNotFoundException;
import com.db.vote.domain.exception.DuplicateVoteException;
import com.db.vote.domain.exception.InvalidAgendaException;
import com.db.vote.domain.exception.VotingSessionAlreadyExistsException;
import com.db.vote.domain.exception.VotingSessionClosedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(InvalidAgendaException.class)
	public ResponseEntity<ErrorResponse> handleInvalidAgenda(InvalidAgendaException exception) {
		log.warn("Rejected invalid agenda: {}", exception.getMessage());
		return ResponseEntity.unprocessableEntity().body(new ErrorResponse(exception.getMessage()));
	}

	@ExceptionHandler(AgendaNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleAgendaNotFound(AgendaNotFoundException exception) {
		log.warn("Agenda not found: {}", exception.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(exception.getMessage()));
	}

	@ExceptionHandler(VotingSessionAlreadyExistsException.class)
	public ResponseEntity<ErrorResponse> handleVotingSessionAlreadyExists(VotingSessionAlreadyExistsException exception) {
		log.warn("Voting session conflict: {}", exception.getMessage());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(exception.getMessage()));
	}

	@ExceptionHandler(VotingSessionClosedException.class)
	public ResponseEntity<ErrorResponse> handleVotingSessionClosed(VotingSessionClosedException exception) {
		log.warn("Rejected vote on closed session: {}", exception.getMessage());
		return ResponseEntity.unprocessableEntity().body(new ErrorResponse(exception.getMessage()));
	}

	@ExceptionHandler(DuplicateVoteException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateVote(DuplicateVoteException exception) {
		log.warn("Rejected duplicate vote: {}", exception.getMessage());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(exception.getMessage()));
	}
}
