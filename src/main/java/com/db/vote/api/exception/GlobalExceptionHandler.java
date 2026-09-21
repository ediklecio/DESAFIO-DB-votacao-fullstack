package com.db.vote.api.exception;

import com.db.vote.api.dto.response.ErrorResponse;
import com.db.vote.domain.exception.InvalidAgendaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
}
