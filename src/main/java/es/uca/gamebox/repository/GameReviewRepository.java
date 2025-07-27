package es.uca.gamebox.repository;

import es.uca.gamebox.entity.Game;
import es.uca.gamebox.entity.GameReview;
import es.uca.gamebox.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameReviewRepository extends JpaRepository<GameReview, UUID> {
    Optional<GameReview> findByUserAndGame(User user, Game game);
    long countByGameAndRecommended(Game game, boolean recommended);
}
