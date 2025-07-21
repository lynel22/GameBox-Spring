package es.uca.gamebox.repository;

import es.uca.gamebox.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AchievementUserRepository extends JpaRepository<AchievementUser, UUID> {
    boolean existsByUserAndAchievement(User user, Achievement achievement);
    boolean existsByUserAndAchievementAndGameUser(User user, Achievement achievement, GameUser gameUser);
    void deleteByGameUserIn(List<GameUser> syncedGameUsers);
    List<AchievementUser> findAllByGameUser_GameAndUser(Game game, User user);
    int countByGameUserId(UUID gameUserId);
}
