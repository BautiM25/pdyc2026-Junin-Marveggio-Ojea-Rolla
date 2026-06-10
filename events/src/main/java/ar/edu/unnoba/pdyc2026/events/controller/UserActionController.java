package ar.edu.unnoba.pdyc2026.events.controller;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unnoba.pdyc2026.events.dto.ArtistResponse;
import ar.edu.unnoba.pdyc2026.events.dto.EventSummary;
import ar.edu.unnoba.pdyc2026.events.dto.FavoriteEventRequest;
import ar.edu.unnoba.pdyc2026.events.dto.FollowArtistRequest;
import ar.edu.unnoba.pdyc2026.events.dto.NotificationResponse;
import ar.edu.unnoba.pdyc2026.events.dto.UpdateNotificationRequest;
import ar.edu.unnoba.pdyc2026.events.service.NotificationService;
import ar.edu.unnoba.pdyc2026.events.service.UserActionService;

@RestController
@RequestMapping("/me")
public class UserActionController {

    @Autowired
    private UserActionService userActionService;

    @Autowired
    private NotificationService notificationService;

    // Método auxiliar privado para no repetir código en cada endpoint
    private String getUsernameFromToken(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return (String) jwtToken.getTokenAttributes().get("preferred_username");
        }
        return authentication.getName();
    }

    @PostMapping("/following")
    public ResponseEntity<Void> followArtist(@RequestBody FollowArtistRequest request, Authentication authentication) 
            throws ExecutionException, InterruptedException {
        String username = getUsernameFromToken(authentication);
        userActionService.followArtist(username, request.artistId()).get();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/following/{artistId}")
    public ResponseEntity<Void> unfollowArtist(@PathVariable Long artistId, Authentication authentication) 
            throws ExecutionException, InterruptedException {
        String username = getUsernameFromToken(authentication);
        userActionService.unfollowArtist(username, artistId).get();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/favorite-events")
    public ResponseEntity<Void> favoriteEvent(@RequestBody FavoriteEventRequest request, Authentication authentication) 
            throws ExecutionException, InterruptedException {
        String username = getUsernameFromToken(authentication);
        userActionService.favoriteEvent(username, request.eventId()).get();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/favorite-events/{eventId}")
    public ResponseEntity<Void> unfavoriteEvent(@PathVariable Long eventId, Authentication authentication) 
            throws ExecutionException, InterruptedException {
        String username = getUsernameFromToken(authentication);
        userActionService.unfavoriteEvent(username, eventId).get();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/following")
    public ResponseEntity<List<ArtistResponse>> getFollowingArtists(Authentication authentication) 
            throws ExecutionException, InterruptedException {
        String username = getUsernameFromToken(authentication);
        List<ArtistResponse> artists = userActionService.getFollowingArtists(username).get();
        return ResponseEntity.ok(artists);
    }

    @GetMapping("/favorite-events")
    public ResponseEntity<List<EventSummary>> getFavoriteEvents(Authentication authentication) 
            throws ExecutionException, InterruptedException {
        String username = getUsernameFromToken(authentication);
        List<EventSummary> events = userActionService.getFavoriteEvents(username).get();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/following/events")
    public ResponseEntity<List<EventSummary>> getFollowingEvents(Authentication authentication) 
            throws ExecutionException, InterruptedException {
        String username = getUsernameFromToken(authentication);
        List<EventSummary> events = userActionService.getFollowingEvents(username).get();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationResponse>> getNotifications(Authentication authentication) 
            throws ExecutionException, InterruptedException {
        String username = getUsernameFromToken(authentication);
        List<NotificationResponse> notifications = notificationService.getMyNotifications(username).get();
        return ResponseEntity.ok(notifications);
    }

    // PATCH /me/notifications/{id} - Marca una notificación como leída
    @PatchMapping("/notifications/{id}")
    public ResponseEntity<Void> updateNotification(
            @PathVariable Long id, 
            @RequestBody UpdateNotificationRequest request, 
            Authentication authentication) throws ExecutionException, InterruptedException {
        
        String username = getUsernameFromToken(authentication);
        if (request.isRead()) {
            notificationService.markAsRead(id, username).get();
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/notifications/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id, Authentication authentication) 
            throws ExecutionException, InterruptedException {
        String username = getUsernameFromToken(authentication);
        notificationService.deleteNotification(id, username).get();
        return ResponseEntity.noContent().build();
    }
}