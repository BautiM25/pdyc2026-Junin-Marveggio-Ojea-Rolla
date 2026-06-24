package ar.edu.unnoba.pdyc2026.events.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ar.edu.unnoba.pdyc2026.events.model.Event;
import ar.edu.unnoba.pdyc2026.events.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsernameIgnoreCase(String username);
    Optional<User> findByEmail(String email);

    // 1. Trae al usuario con la colección 'following' inicializada
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.following WHERE LOWER(u.username) = LOWER(:username)")
    Optional<User> findByUsernameWithFollowingIgnoreCase(@Param("username") String username);

    // 2. Trae al usuario con la colección 'favoriteEvents' inicializada
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.favoriteEvents WHERE LOWER(u.username) = LOWER(:username)")
    Optional<User> findByUsernameWithFavoriteEventsIgnoreCase(@Param("username") String username);

    // 3. Trae al usuario con 'following' y además los eventos de esos artistas (para getFollowingEvents)
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.following f LEFT JOIN FETCH f.events WHERE LOWER(u.username) = LOWER(:username)")
    Optional<User> findByUsernameWithFollowingAndEventsIgnoreCase(@Param("username") String username);

    // Cambiamos 'JOIN u.following a WHERE a = :artist' por 'WHERE a.id = :artistId'
    @Query("SELECT u FROM User u JOIN u.following a WHERE a.id = :artistId")
    List<User> findByFollowingArtistId(@Param("artistId") Long artistId);

    // Mantenemos este igual, ya que el evento sí se guardó justo antes
    @Query("SELECT u FROM User u JOIN u.favoriteEvents e WHERE e = :event")
    Set<User> findByFavoriteEventsContains(@Param("event") Event event);
}