package es.uca.gamebox.repository;

import es.uca.gamebox.entity.Developer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeveloperRepository extends JpaRepository<Developer, UUID> {
    Optional<Developer> findByNameIgnoreCase(String name);
}
