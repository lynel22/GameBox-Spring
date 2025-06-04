package es.uca.gamebox.repository;

import es.uca.gamebox.entity.Game;
import es.uca.gamebox.entity.GameUser;
import es.uca.gamebox.entity.Library;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface GameUserRepository extends JpaRepository<GameUser, UUID> {
    boolean existsByLibraryAndGame(Library steamLibrary, Game game);
    List<GameUser> findByLibraryInAndSyncedTrue(Collection<@NotNull Library> library);
}
