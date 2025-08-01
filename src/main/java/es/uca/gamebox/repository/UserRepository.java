package es.uca.gamebox.repository;

import es.uca.gamebox.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByVerificationToken(String token);
    Optional<User> findByPasswordResetToken(String token);
    Optional<List<User>> findBySteamIdIn(List<String> steamIds);

    /*@EntityGraph(attributePaths = {
            "friends",
            "friends.libraries",
            "friends.libraries.gameUsers",
            "friends.libraries.gameUsers.game"
    })
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithFriendsAndGames(UUID id);*/

    @EntityGraph(attributePaths = {"friends"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithFriends(@Param("id") UUID id);

    @EntityGraph(attributePaths = {
            "libraries",
            "libraries.gameUsers",
            "libraries.gameUsers.game",
            "libraries.store"
    })
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithLibraries(@Param("id") UUID id);

    boolean existsByFriendCode(String generatedCode);

    Optional<User> findByFriendCode(String friendCode);
}
