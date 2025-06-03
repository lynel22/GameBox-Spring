package es.uca.gamebox.repository;

import es.uca.gamebox.entity.Game;
import es.uca.gamebox.entity.GameUser;
import es.uca.gamebox.entity.Library;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GameUserRepository extends JpaRepository<GameUser, UUID> {
    boolean existsByLibraryAndGame(Library steamLibrary, Game game);
    // Custom query methods can be defined here if needed
}
