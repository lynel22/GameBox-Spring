package es.uca.gamebox.repository;

import es.uca.gamebox.entity.Game;
import es.uca.gamebox.entity.User;
import es.uca.gamebox.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WishlistRepository extends JpaRepository<Wishlist, UUID> {
    Optional<Wishlist> findByUserAndGame(User user, Game game);
    boolean existsByUserAndGame(User user, Game game);

    List<Wishlist> findByUser(User user);
}
