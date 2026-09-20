package com.db.vote.domain;

import com.db.vote.domain.exception.InvalidAgendaException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "agendas")
public class Agenda {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	private String description;

	protected Agenda() {
	}

	public Agenda(String title, String description) {
		if (title == null || title.isBlank()) {
			throw new InvalidAgendaException("Agenda title must not be null or blank");
		}
		this.title = title;
		this.description = description;
	}

	public Agenda(Long id, String title, String description) {
		this(title, description);
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}
}
