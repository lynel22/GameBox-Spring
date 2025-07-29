package es.uca.gamebox.repository;

import es.uca.gamebox.entity.Game;
import es.uca.gamebox.entity.GameReview;
import es.uca.gamebox.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameReviewRepository extends JpaRepository<GameReview, UUID> {
    Optional<GameReview> findByUserAndGame(User user, Game game);
    long countByGameAndRecommended(Game game, boolean recommended);
    @Query("SELECT gr.game.id AS gameId, COUNT(gr) AS positiveCount " +
            "FROM GameReview gr WHERE gr.recommended = true GROUP BY gr.game.id")
    List<Map<String, Object>> findGameRecommendationCounts();
}
