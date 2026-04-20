package ar.edu.unnoba.pdyc2026.events.dto;

import ar.edu.unnoba.pdyc2026.events.model.EventStatus;
import java.time.LocalDate;

public record EventSummary(Long id, String name, LocalDate startDate, EventStatus status, int artistCount) {}