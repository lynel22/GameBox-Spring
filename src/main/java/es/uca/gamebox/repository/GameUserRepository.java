package es.uca.gamebox.repository;

import es.uca.gamebox.entity.Game;
import es.uca.gamebox.entity.GameUser;
import es.uca.gamebox.entity.Library;
import es.uca.gamebox.entity.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface GameUserRepository extends JpaRepository<GameUser, UUID> {
    boolean existsByLibraryAndGame(Library steamLibrary, Game game);
    List<GameUser> findByLibraryInAndSyncedTrue(Collection<@NotNull Library> library);
    List<GameUser> findByLibrary(Library library);

    @Query("SELECT DISTINCT gu.game FROM GameUser gu WHERE gu.library.user = :user")
    List<Game> findGamesByUser(@Param("user") User user);

    @Query("""
    SELECT DISTINCT gu.game
    FROM GameUser gu
    WHERE gu.library.user = :user AND gu.library.store.name = :storeName
""")
    List<Game> findGamesByUserAndStoreName(@Param("user") User user, @Param("storeName") String storeName);

}
