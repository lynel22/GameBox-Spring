package es.uca.gamebox.repository;

import es.uca.gamebox.dto.LibraryGameCountDto;
import es.uca.gamebox.entity.Game;
import es.uca.gamebox.entity.GameUser;
import es.uca.gamebox.entity.Library;
import es.uca.gamebox.entity.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface GameUserRepository extends JpaRepository<GameUser, UUID> {
    boolean existsByLibraryAndGame(Library steamLibrary, Game game);
    List<GameUser> findByLibraryInAndSyncedTrue(Collection<@NotNull Library> library);
    List<GameUser> findByLibrary(Library library);

    @Query("SELECT DISTINCT gu.game FROM GameUser gu WHERE gu.library.user = :user")
    List<Game> findGamesByUser(@Param("user") User user);

    @Query("""
    SELECT DISTINCT gu.game
    FROM GameUser gu
    WHERE gu.library.user = :user AND gu.library.store.name = :storeName""")
    List<Game> findGamesByUserAndStoreName(@Param("user") User user, @Param("storeName") String storeName);

    @Query("SELECT gu FROM GameUser gu WHERE gu.game.id = :gameId AND gu.library.user.id = :userId")
    Optional<GameUser> findByUserAndGame(@Param("userId") UUID userId, @Param("gameId") UUID gameId);

    @Query("""
        SELECT new es.uca.gamebox.dto.LibraryGameCountDto(s.name, COUNT(gu))
        FROM GameUser gu
        JOIN gu.library l
        JOIN l.store s
        WHERE l.user = :user
        GROUP BY s.name
    """)
    List<LibraryGameCountDto> countGamesGroupedByStore(@Param("user") User user);

    List<GameUser> findAllByLibraryUserIdAndGameId(UUID userId, UUID gameId);

    boolean existsByLibrary_UserAndGame(User user, Game game);
}
