package com.db.vote.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateAgendaRequest (
        @NotBlank(message = "Título da pauta é obrigatório") String title,
        String description
) { }
