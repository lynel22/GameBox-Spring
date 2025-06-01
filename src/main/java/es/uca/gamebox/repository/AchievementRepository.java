package es.uca.gamebox.repository;

import es.uca.gamebox.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AchievementRepository extends JpaRepository<Achievement, UUID> {
    Optional<Achievement> findByNameIgnoreCase(String name);
}
