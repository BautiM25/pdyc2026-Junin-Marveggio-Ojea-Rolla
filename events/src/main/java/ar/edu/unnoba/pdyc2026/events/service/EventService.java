package ar.edu.unnoba.pdyc2026.events.service;

import ar.edu.unnoba.pdyc2026.events.model.Artist;
import ar.edu.unnoba.pdyc2026.events.model.Event;
import ar.edu.unnoba.pdyc2026.events.model.EventStatus;
import ar.edu.unnoba.pdyc2026.events.repository.ArtistRepository;
import ar.edu.unnoba.pdyc2026.events.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ArtistRepository artistRepository;

    public List<Event> findAll(Optional<EventStatus> state) {
        return state.map(eventRepository::findByStatus)
                    .orElseGet(eventRepository::findAll);
    }

    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }

    @Transactional
    public Event createEvent(String name, LocalDate startDate, String description) {
        Event event = new Event();
        event.setName(name);
        event.setStartDate(startDate);
        event.setDescription(description);
        event.setStatus(EventStatus.tentative);
        return eventRepository.save(event);
    }

    @Transactional
    public Event updateEvent(Long id, String name, LocalDate startDate, String description) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        if (event.getStatus() != EventStatus.tentative) {
            throw new IllegalStateException("Only tentative events can be updated");
        }
        event.setName(name);
        event.setStartDate(startDate);
        event.setDescription(description);
        return eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        if (event.getStatus() != EventStatus.tentative) {
            throw new IllegalStateException("Only tentative events can be deleted");
        }
        eventRepository.delete(event);
    }

    @Transactional
    public void addArtistToEvent(Long eventId, Long artistId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new RuntimeException("Artist not found"));

        if (event.getStatus() != EventStatus.tentative) {
            throw new IllegalStateException("Can only add artists to tentative events");
        }
        if (!artist.isActive()) {
            throw new IllegalStateException("Artist is deactivated and cannot be added to events");
        }
        if (event.getArtists().contains(artist)) {
            throw new IllegalStateException("Artist already in event");
        }
        event.getArtists().add(artist);
        eventRepository.save(event);
    }

    @Transactional
    public void removeArtistFromEvent(Long eventId, Long artistId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new RuntimeException("Artist not found"));

        if (event.getStatus() != EventStatus.tentative) {
            throw new IllegalStateException("Can only remove artists from tentative events");
        }
        if (!event.getArtists().contains(artist)) {
            throw new IllegalStateException("Artist not in event");
        }
        event.getArtists().remove(artist);
        eventRepository.save(event);
    }

    @Transactional
    public void confirmEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        if (event.getStatus() != EventStatus.tentative) {
            throw new IllegalStateException("Only tentative events can be confirmed");
        }
        if (!event.getStartDate().isAfter(LocalDate.now())) {
            throw new IllegalStateException("Cannot confirm event with past or today's date");
        }
        event.setStatus(EventStatus.confirmed);
        eventRepository.save(event);
    }

    @Transactional
    public void rescheduleEvent(Long eventId, LocalDate newStartDate) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        if (event.getStatus() != EventStatus.confirmed && event.getStatus() != EventStatus.rescheduled) {
            throw new IllegalStateException("Only confirmed or rescheduled events can be rescheduled");
        }
        if (!event.getStartDate().isAfter(LocalDate.now()) && !event.getStartDate().isEqual(LocalDate.now())) {
            throw new IllegalStateException("Only events with future or today's date can be rescheduled");
        }
        if (!newStartDate.isAfter(LocalDate.now())) {
            throw new IllegalStateException("New date must be in the future");
        }
        event.setStartDate(newStartDate);
        event.setStatus(EventStatus.rescheduled);
        eventRepository.save(event);
    }

    @Transactional
    public void cancelEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        if (event.getStatus() != EventStatus.confirmed && event.getStatus() != EventStatus.rescheduled) {
            throw new IllegalStateException("Only confirmed or rescheduled events can be cancelled");
        }
        event.setStatus(EventStatus.cancelled);
        eventRepository.save(event);
    }
}