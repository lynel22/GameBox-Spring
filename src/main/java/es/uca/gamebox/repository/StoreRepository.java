package es.uca.gamebox.repository;

import es.uca.gamebox.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {
    Optional<Store> findBySlugRawg(String slug);
    Optional<Store> findByRawgId(int rawgId);
    Optional<Store> findByNameIgnoreCase(String name);
}
