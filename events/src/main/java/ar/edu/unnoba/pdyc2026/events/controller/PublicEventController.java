package ar.edu.unnoba.pdyc2026.events.controller;

import ar.edu.unnoba.pdyc2026.events.dto.EventDetail;
import ar.edu.unnoba.pdyc2026.events.dto.EventSummary;
import ar.edu.unnoba.pdyc2026.events.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/events")
public class PublicEventController {

    @Autowired
    private EventService eventService;

    // GET /events
    @GetMapping
    public ResponseEntity<List<EventSummary>> getPublicEvents() {
        return ResponseEntity.ok(eventService.findPublicEvents());
    }

    // GET /events/{eventId}
    @GetMapping("/{eventId}")
    public ResponseEntity<EventDetail> getPublicEventDetail(@PathVariable Long eventId) {
        try {
            EventDetail detail = eventService.findPublicEventDetail(eventId);
            return ResponseEntity.ok(detail);
        } catch (RuntimeException e) {
            // Si el evento es 'tentative' u ocurrió un error, devolvemos un 403 Forbidden para ocultar la info
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}