package es.uca.gamebox.repository;

import es.uca.gamebox.entity.Platform;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlatformRepository extends JpaRepository<Platform, UUID> {
    Optional<Platform> findByNameIgnoreCase(String name);
}
