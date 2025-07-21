package es.uca.gamebox.repository;

import es.uca.gamebox.entity.Achievement;
import es.uca.gamebox.entity.Game;
import es.uca.gamebox.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AchievementRepository extends JpaRepository<Achievement, UUID> {
    List<Achievement> findByGameAndNameIn(Game game, List<String> names);
    boolean existsByGameAndName(Game game, String name);
    List<Achievement> findAllByGame(Game game);
    int countByGame(Game game);
}
