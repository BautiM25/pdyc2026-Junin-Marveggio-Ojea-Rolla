package ar.edu.unnoba.pdyc2026.events.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unnoba.pdyc2026.events.dto.NotificationResponse;
import ar.edu.unnoba.pdyc2026.events.model.Artist;
import ar.edu.unnoba.pdyc2026.events.model.Event;
import ar.edu.unnoba.pdyc2026.events.model.EventStatus;
import ar.edu.unnoba.pdyc2026.events.model.Notification;
import ar.edu.unnoba.pdyc2026.events.model.User;
import ar.edu.unnoba.pdyc2026.events.repository.NotificationRepository;
import ar.edu.unnoba.pdyc2026.events.repository.UserRepository;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Dispara de forma asíncrona la creación de alertas para los usuarios correspondientes.
     */
    @Transactional
    public CompletableFuture<Void> createNotificationsForEventChange(Event event, EventStatus previousState, EventStatus currentState) {
        return CompletableFuture.runAsync(() -> {
            Set<User> usersToNotify = new HashSet<>();

            // 1. Usuarios que tienen el evento en favoritos
            List<User> fans = userRepository.findByFavoriteEventsContains(event);
            usersToNotify.addAll(fans);

            // 2. Usuarios que siguen a al menos un artista participante (POR ID)
            for (Artist artist : event.getArtists()) {
                List<User> followers = userRepository.findByFollowingArtistId(artist.getId());
                usersToNotify.addAll(followers);
            }

            // 3. Crear y guardar las instancias de Notification
            List<Notification> notifications = usersToNotify.stream().map(user -> {
                Notification notification = new Notification();
                notification.setEvent(event);
                notification.setUser(user);
                notification.setPreviousState(previousState);
                notification.setCurrentState(currentState);
                return notification;
            }).collect(Collectors.toList());

            if (!notifications.isEmpty()) {
                notificationRepository.saveAll(notifications);
            }
        });
    }

    @Transactional(readOnly = true)
    public CompletableFuture<List<NotificationResponse>> getMyNotifications(String username) {
        return CompletableFuture.supplyAsync(() -> {
            User user = userRepository.findByUsernameIgnoreCase(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Usamos el nuevo método con JOIN FETCH para evitar el (no session) del Event
            return notificationRepository.findByUserWithEventOrderByCreatedAtDesc(user).stream()
                    .map(n -> new NotificationResponse(
                            n.getId(),
                            n.getEvent().getId(),
                            n.getEvent().getName(),
                            n.getPreviousState(),
                            n.getCurrentState(),
                            n.getCreatedAt(),
                            n.isRead()
                    ))
                    .collect(Collectors.toList());
        });
    }

    @Transactional
    public CompletableFuture<Void> markAsRead(Long notificationId, String username) {
        return CompletableFuture.runAsync(() -> {
            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new RuntimeException("Notification not found"));
            
            if (!notification.getUser().getUsername().equalsIgnoreCase(username)) {
                throw new RuntimeException("Unauthorized to update this notification");
            }

            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    @Transactional
    public CompletableFuture<Void> deleteNotification(Long notificationId, String username) {
        return CompletableFuture.runAsync(() -> {
            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new RuntimeException("Notification not found"));
            
            if (!notification.getUser().getUsername().equalsIgnoreCase(username)) {
                throw new RuntimeException("Unauthorized to delete this notification");
            }

            notificationRepository.delete(notification);
        });
    }
}