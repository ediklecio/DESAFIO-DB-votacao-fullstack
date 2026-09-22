package com.db.vote.api.dto.response;

import com.db.vote.domain.Agenda;

public record AgendaResponse(Long id, String title, String description) {

	public static AgendaResponse from(Agenda agenda) {
		return new AgendaResponse(agenda.getId(), agenda.getTitle(), agenda.getDescription());
	}
}
