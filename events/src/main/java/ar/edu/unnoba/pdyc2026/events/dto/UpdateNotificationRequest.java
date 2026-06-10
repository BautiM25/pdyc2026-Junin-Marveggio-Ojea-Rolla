package ar.edu.unnoba.pdyc2026.events.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateNotificationRequest(
    @JsonProperty("is_read") boolean isRead
) {}