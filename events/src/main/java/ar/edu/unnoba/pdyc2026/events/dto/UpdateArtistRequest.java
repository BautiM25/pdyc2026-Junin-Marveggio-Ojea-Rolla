package ar.edu.unnoba.pdyc2026.events.dto;

import ar.edu.unnoba.pdyc2026.events.model.Genre;

public record UpdateArtistRequest(String name, Genre genre) {}