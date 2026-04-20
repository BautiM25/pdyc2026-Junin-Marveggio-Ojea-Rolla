package ar.edu.unnoba.pdyc2026.events.dto;

import ar.edu.unnoba.pdyc2026.events.model.EventStatus;
import java.time.LocalDate;
import java.util.List;

public record EventDetail(Long id, String name, String description, LocalDate startDate,
                          EventStatus status, List<ArtistResponse> artists) {}