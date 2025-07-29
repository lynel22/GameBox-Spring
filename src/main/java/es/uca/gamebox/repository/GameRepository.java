package es.uca.gamebox.repository;

import es.uca.gamebox.entity.Game;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameRepository extends JpaRepository<Game, UUID> {
    Optional<Game> findByName(String name);
    List<Game> findBySteamAppIdIn(List<String> appIds);
    Optional<Game> findByRawgSlug(String rawgSlug);
    List<Game> findBySteamAppIdNotNullAndSteamAchievementsSyncedFalse();

    @EntityGraph(attributePaths = {
            "developer",
            "genres",
            "platforms",
            "stores",
            "deals",
            "deals.store"
    })
    Optional<Game> findWithDetailsById(UUID id);
    List<Game> findTop20ByNameContainingIgnoreCase(String name);
    Optional<Game> findByNameIgnoreCase(String title);
    List<Game> findAllByGenres_Id(UUID genreId);
}
