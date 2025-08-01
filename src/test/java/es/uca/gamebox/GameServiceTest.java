package es.uca.gamebox;

import es.uca.gamebox.entity.*;
import es.uca.gamebox.repository.*;
import es.uca.gamebox.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameServiceTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameUserRepository gameUserRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameReviewRepository gameReviewRepository;

    @Autowired
    private AchievementUserRepository userAchievementRepository;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private AchievementUserRepository achievementUserRepository;

    private User testUser;
    private Game testGame;

    @BeforeEach
    public void cleanUp() {
        String testEmail = "test-user@example.com";
        testUser = userRepository.findByEmail(testEmail).orElseThrow();

        // Elimina relaciones del usuario test
        gameReviewRepository.deleteAllByUser(testUser);
        gameUserRepository.deleteAllByLibrary_User(testUser);
        userAchievementRepository.deleteAllByUser(testUser);
        recommendationRepository.deleteAllByUser(testUser);
        wishlistRepository.deleteAllByUser(testUser);

        testGame = gameRepository.findByName("Hades").orElseThrow(); // cambia si no coincide el nombre
    }

    @Test
    public void testAddGameToWishlist() {
        gameService.addGameToWishlist(testGame.getId(), testUser);

        var wishlist = wishlistRepository.findByUserAndGame(testUser, testGame);
        assertTrue(wishlist.isPresent(), "El juego debería estar en la wishlist");

        assertThrows(RuntimeException.class, () -> {
            gameService.addGameToWishlist(testGame.getId(), testUser);
        }, "Debería lanzar excepción al intentar añadir un juego ya existente en la wishlist");
    }

    @Test
    public void testRemoveGameFromWishlist() {
        // Añadir primero
        gameService.addGameToWishlist(testGame.getId(), testUser);

        // Eliminar
        gameService.removeGameFromWishlist(testGame.getId(), testUser);
        var wishlist = wishlistRepository.findByUserAndGame(testUser, testGame);
        assertTrue(wishlist.isEmpty(), "El juego debería haber sido eliminado de la wishlist");
    }

    @Test
    public void testAddGameToLibraries() {
        Store steam = storeRepository.findByName("Steam")
                .orElseThrow(() -> new RuntimeException("Store Steam no encontrada"));

        List<UUID> storeIds = List.of(steam.getId());

        gameService.addGameToLibraries(testGame.getId(), storeIds, testUser);

        // Verificar que el juego está en la biblioteca
        boolean exists = gameUserRepository.existsByLibrary_UserAndGame(testUser, testGame);
        assertTrue(exists, "El juego debería estar en la biblioteca del usuario");
    }

    @Test
    public void testReviewGameAfterAddingToLibrary() {
        Store steam = storeRepository.findByName("Steam")
                .orElseThrow(() -> new RuntimeException("Store Steam no encontrada"));

        // Añadir juego a la biblioteca usando el servicio
        gameService.addGameToLibraries(testGame.getId(), List.of(steam.getId()), testUser);

        // Realizar reseña (true = recomendado)
        gameService.reviewGame(testUser, testGame.getId(), true);

        // Verificar que se haya creado la reseña correctamente
        Optional<GameReview> reviewOpt = gameReviewRepository.findByUserAndGame(testUser, testGame);
        assertTrue(reviewOpt.isPresent(), "La reseña debería haberse creado");
        assertTrue(reviewOpt.get().isRecommended(), "La reseña debería indicar que es recomendado");
    }

    @Test
    @Transactional
    public void testAddAchievementToGame() {
        // Buscar un logro ya existente
        Achievement achievement = achievementRepository.findAll().stream()
                .filter(a -> a.getGame().getId().equals(testGame.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No hay logros precargados para este juego"));

        Store steam = storeRepository.findByName("Steam")
                .orElseThrow(() -> new RuntimeException("Store Steam no encontrada"));

        gameService.addGameToLibraries(testGame.getId(), List.of(steam.getId()), testUser);

        gameService.addAchievementToGame(testUser, testGame.getId(), achievement.getId());

        // Verificar que se creó un AchievementUser correctamente
        List<AchievementUser> achievementUsers = achievementUserRepository.findAllByUserId(testUser.getId());
        assertEquals(1, achievementUsers.size(), "Debería haberse creado un logro desbloqueado");

        AchievementUser result = achievementUsers.getFirst();
        assertEquals(achievement.getId(), result.getAchievement().getId());
        assertEquals(testUser.getId(), result.getUser().getId());
        assertNotNull(result.getGameUser(), "Debe tener asociada una instancia GameUser");
        assertNotNull(result.getDateUnlocked(), "Debe tener fecha de desbloqueo");

        // Si intentamos añadir el mismo logro otra vez, debe lanzar excepción
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            gameService.addAchievementToGame(testUser, testGame.getId(), achievement.getId());
        });
        assertEquals("Achievement already unlocked", ex.getMessage());
    }

}
