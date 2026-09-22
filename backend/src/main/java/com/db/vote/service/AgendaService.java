package com.db.vote.service;

import com.db.vote.api.dto.request.CreateAgendaRequest;
import com.db.vote.domain.Agenda;
import com.db.vote.domain.exception.InvalidAgendaException;
import com.db.vote.repository.AgendaRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AgendaService {

	private static final Logger log = LoggerFactory.getLogger(AgendaService.class);

	private final AgendaRepository agendaRepository;
	private final Validator validator;

	public Agenda createAgenda(CreateAgendaRequest request) {
		Set<ConstraintViolation<CreateAgendaRequest>> violations = validator.validate(request);
		if (!violations.isEmpty()) {
			throw new InvalidAgendaException(violations.iterator().next().getMessage());
		}

		Agenda agenda = new Agenda(null, request.title(), request.description());
		Agenda savedAgenda = agendaRepository.save(agenda);
		log.info("Agenda created: id={}, title={}", savedAgenda.getId(), savedAgenda.getTitle());
		return savedAgenda;
	}
}
