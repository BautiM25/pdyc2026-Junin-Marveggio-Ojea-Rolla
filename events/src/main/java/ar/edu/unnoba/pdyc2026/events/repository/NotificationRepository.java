package ar.edu.unnoba.pdyc2026.events.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ar.edu.unnoba.pdyc2026.events.model.Notification;
import ar.edu.unnoba.pdyc2026.events.model.User;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Usamos JOIN FETCH para traer el Event junto a la notificación de un solo viaje
    @Query("SELECT n FROM Notification n JOIN FETCH n.event WHERE n.user = :user ORDER BY n.createdAt DESC")
    List<Notification> findByUserWithEventOrderByCreatedAtDesc(@Param("user") User user);

    // Usamos JOIN FETCH para Para traer la notificación con el usuario ya cargado de un solo viaje
    @Query("SELECT n FROM Notification n JOIN FETCH n.user WHERE n.id = :id")
    Optional<Notification> findByIdWithUser(@Param("id") Long id);
}