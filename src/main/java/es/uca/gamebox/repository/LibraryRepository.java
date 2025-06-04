package es.uca.gamebox.repository;

import es.uca.gamebox.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LibraryRepository extends JpaRepository<Library, UUID> {
    List<Library> findByUser(User user);

    List<Library> findByUserIdAndStore(UUID userId, Store store);
}
