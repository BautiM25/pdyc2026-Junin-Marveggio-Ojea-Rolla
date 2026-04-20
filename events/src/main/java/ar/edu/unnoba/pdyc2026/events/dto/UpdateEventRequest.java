package ar.edu.unnoba.pdyc2026.events.dto;

import java.time.LocalDate;

public record UpdateEventRequest(String name, LocalDate start_date, String description) {}