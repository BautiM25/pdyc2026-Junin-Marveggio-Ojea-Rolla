package ar.edu.unnoba.pdyc2026.events.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unnoba.pdyc2026.events.dto.ArtistResponse;
import ar.edu.unnoba.pdyc2026.events.dto.EventSummary;
import ar.edu.unnoba.pdyc2026.events.model.Artist;
import ar.edu.unnoba.pdyc2026.events.model.Event;
import ar.edu.unnoba.pdyc2026.events.model.EventStatus;
import ar.edu.unnoba.pdyc2026.events.model.User;
import ar.edu.unnoba.pdyc2026.events.repository.ArtistRepository;
import ar.edu.unnoba.pdyc2026.events.repository.EventRepository;
import ar.edu.unnoba.pdyc2026.events.repository.UserRepository;

@Service
public class UserActionService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private EventRepository eventRepository;

    // Método auxiliar modificado para normalizar el registro inicial en minúsculas
    private User getOrCreateUser(String username) {
        String normalized = username.toLowerCase();
        return userRepository.findByUsernameIgnoreCase(normalized)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(normalized);
                    newUser.setEmail(normalized + "@example.com"); 
                    return userRepository.save(newUser);
                });
    }

    @Transactional
    public CompletableFuture<Void> followArtist(String username, Long artistId) {
        return CompletableFuture.runAsync(() -> {
            // Usamos JOIN FETCH para 'following'
            User user = userRepository.findByUsernameWithFollowingIgnoreCase(username)
                    .orElseGet(() -> getOrCreateUser(username));
            
            Artist artist = artistRepository.findByIdAndActiveTrue(artistId)
                    .orElseThrow(() -> new RuntimeException("Active artist not found: " + artistId));
            
            user.getFollowing().add(artist);
            userRepository.save(user);
        });
    }

    @Transactional
    public CompletableFuture<Void> unfollowArtist(String username, Long artistId) {
        return CompletableFuture.runAsync(() -> {
            // Usamos JOIN FETCH para 'following'
            User user = userRepository.findByUsernameWithFollowingIgnoreCase(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            Artist artist = artistRepository.findById(artistId)
                    .orElseThrow(() -> new RuntimeException("Artist not found: " + artistId));
            
            user.getFollowing().remove(artist);
            userRepository.save(user);
        });
    }

    @Transactional
    public CompletableFuture<Void> favoriteEvent(String username, Long eventId) {
        return CompletableFuture.runAsync(() -> {
            // Usamos JOIN FETCH para 'favoriteEvents'
            User user = userRepository.findByUsernameWithFavoriteEventsIgnoreCase(username)
                    .orElseGet(() -> getOrCreateUser(username));
            
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));
            
            user.getFavoriteEvents().add(event);
            userRepository.save(user);
        });
    }

    @Transactional
    public CompletableFuture<Void> unfavoriteEvent(String username, Long eventId) {
        return CompletableFuture.runAsync(() -> {
            // Usamos JOIN FETCH para 'favoriteEvents'
            User user = userRepository.findByUsernameWithFavoriteEventsIgnoreCase(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));
            
            user.getFavoriteEvents().remove(event);
            userRepository.save(user);
        });
    }

    @Transactional(readOnly = true)
    public CompletableFuture<List<ArtistResponse>> getFollowingArtists(String username) {
        return CompletableFuture.supplyAsync(() -> {
            // Usamos JOIN FETCH para 'following'
            User user = userRepository.findByUsernameWithFollowingIgnoreCase(username)
                    .orElseGet(() -> getOrCreateUser(username));
            
            return user.getFollowing().stream()
                    .map(artist -> new ArtistResponse(
                            artist.getId(),
                            artist.getName(),
                            artist.getGenre(),
                            artist.isActive()
                    ))
                    .collect(Collectors.toList());
        });
    }

    @Transactional(readOnly = true)
    public CompletableFuture<List<EventSummary>> getFavoriteEvents(String username) {
        return CompletableFuture.supplyAsync(() -> {
            // Usamos JOIN FETCH para 'favoriteEvents'
            User user = userRepository.findByUsernameWithFavoriteEventsIgnoreCase(username)
                    .orElseGet(() -> getOrCreateUser(username));
            LocalDate today = LocalDate.now();

            return user.getFavoriteEvents().stream()
                    .filter(event -> !event.getStartDate().isBefore(today))
                    .filter(event -> event.getStatus() != EventStatus.tentative)
                    .map(event -> new EventSummary(
                            event.getId(),
                            event.getName(),
                            event.getStartDate(),
                            event.getStatus(),
                            event.getArtists().size()
                    ))
                    .collect(Collectors.toList());
        });
    }

    @Transactional(readOnly = true)
    public CompletableFuture<List<EventSummary>> getFollowingEvents(String username) {
        return CompletableFuture.supplyAsync(() -> {
            // Usamos el JOIN FETCH anidado (User -> Following -> Events) para evitar cargar de forma diferida en el stream
            User user = userRepository.findByUsernameWithFollowingAndEventsIgnoreCase(username)
                    .orElseGet(() -> getOrCreateUser(username));
            LocalDate today = LocalDate.now();

            return user.getFollowing().stream()
                    .flatMap(artist -> artist.getEvents().stream())
                    .distinct()
                    .filter(event -> !event.getStartDate().isBefore(today))
                    .filter(event -> event.getStatus() != EventStatus.tentative)
                    .sorted(Comparator.comparing(Event::getStartDate))
                    .map(event -> new EventSummary(
                            event.getId(),
                            event.getName(),
                            event.getStartDate(),
                            event.getStatus(),
                            event.getArtists().size()
                    ))
                    .collect(Collectors.toList());
        });
    }
}