package es.uca.gamebox.repository;

import es.uca.gamebox.entity.Deal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DealRepository extends JpaRepository<Deal, UUID> {
    List<Deal> findByEndDateIsNull();
    List<Deal> findByEndDateIsNullAndStore_NameIgnoreCase(String storeName);
    List<Deal> findAllByLastSeenBeforeAndEndDateIsNull(Instant threshold);
    Optional<Deal> findByCheapSharkID(String dealID);

}
