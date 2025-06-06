package es.uca.gamebox.repository;

import es.uca.gamebox.entity.Achievement;
import es.uca.gamebox.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AchievementRepository extends JpaRepository<Achievement, UUID> {
    Optional<Achievement> findByNameIgnoreCase(String name);
    List<Achievement> findByGameAndNameIn(Game game, List<String> names);
    boolean existsByGameAndName(Game game, String name);
}
