package ar.edu.unnoba.pdyc2026.events.repository;

import ar.edu.unnoba.pdyc2026.events.model.Artist;
import ar.edu.unnoba.pdyc2026.events.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
    List<Artist> findByGenre(Genre genre);
    Optional<Artist> findByIdAndActiveTrue(Long id);
}