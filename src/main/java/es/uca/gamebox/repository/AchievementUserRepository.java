package es.uca.gamebox.repository;

import es.uca.gamebox.entity.Achievement;
import es.uca.gamebox.entity.AchievementUser;
import es.uca.gamebox.entity.GameUser;
import es.uca.gamebox.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AchievementUserRepository extends JpaRepository<AchievementUser, UUID> {
    boolean existsByUserAndAchievement(User user, Achievement achievement);
    boolean existsByUserAndAchievementAndGameUser(User user, Achievement achievement, GameUser gameUser);
    void deleteByGameUserIn(List<GameUser> syncedGameUsers);
}
