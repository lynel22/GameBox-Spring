package es.uca.gamebox.repository;

import es.uca.gamebox.entity.Recommendation;
import es.uca.gamebox.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RecommendationRepository extends JpaRepository<Recommendation, UUID> {
    List<Recommendation> findByUserAndRecommendationDate(User user, LocalDate date);
    void deleteByRecommendationDate(LocalDate date);
    void deleteAllByUser(User testUser);
}
