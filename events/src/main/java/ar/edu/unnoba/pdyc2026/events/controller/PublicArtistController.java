package ar.edu.unnoba.pdyc2026.events.controller;

import ar.edu.unnoba.pdyc2026.events.dto.ArtistResponse;
import ar.edu.unnoba.pdyc2026.events.dto.EventSummary;
import ar.edu.unnoba.pdyc2026.events.service.ArtistService;
import ar.edu.unnoba.pdyc2026.events.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/artists")
public class PublicArtistController {

    @Autowired
    private ArtistService artistService;

    @Autowired
    private EventService eventService;

    // GET /artists
    @GetMapping
    public ResponseEntity<List<ArtistResponse>> getActiveArtists() {
        return ResponseEntity.ok(artistService.findActiveArtists());
    }

    // GET /artists/{artistId}/events
    @GetMapping("/{artistId}/events")
    public ResponseEntity<List<EventSummary>> getArtistPublicEvents(@PathVariable Long artistId) {
        return ResponseEntity.ok(eventService.findPublicEventsByArtist(artistId));
    }
}