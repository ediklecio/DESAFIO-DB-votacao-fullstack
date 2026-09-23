package com.db.vote.api.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Stable JSON shape for paginated listings, instead of serializing Spring's
 * PageImpl directly (whose JSON structure is not a supported contract).
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

	public static <T> PageResponse<T> from(Page<T> page) {
		return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
				page.getTotalElements(), page.getTotalPages());
	}
}
