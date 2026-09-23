package com.db.vote.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateAgendaRequest(
		@NotBlank(message = "Agenda title must not be blank") String title,
		String description) {
}
