package es.uca.gamebox.repository;

import es.uca.gamebox.entity.Game;
import es.uca.gamebox.entity.Library;
import es.uca.gamebox.entity.Platform;
import es.uca.gamebox.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LibraryRepository extends JpaRepository<Library, UUID> {
    List<Library> findByUser(User user);
    Optional<Library> findByUserAndPlatform(User currentUser, Platform steamPlatform);
}
