package es.uca.gamebox;

import es.uca.gamebox.entity.*;
import es.uca.gamebox.repository.*;
import es.uca.gamebox.service.GameService;
import org.junit.jupiter.api.*;
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

    @Autowired private GameService gameService;
    @Autowired private UserRepository userRepository;
    @Autowired private GameUserRepository gameUserRepository;
    @Autowired private GameRepository gameRepository;
    @Autowired private GameReviewRepository gameReviewRepository;
    @Autowired private AchievementUserRepository userAchievementRepository;
    @Autowired private RecommendationRepository recommendationRepository;
    @Autowired private WishlistRepository wishlistRepository;
    @Autowired private StoreRepository storeRepository;
    @Autowired private AchievementRepository achievementRepository;
    @Autowired private AchievementUserRepository achievementUserRepository;

    private User testUser;
    private Game testGame;

    @BeforeEach
    public void cleanUp() {
        testUser = userRepository.findByEmail("test-user@example.com").orElseThrow();
        gameReviewRepository.deleteAllByUser(testUser);
        gameUserRepository.deleteAllByLibrary_User(testUser);
        userAchievementRepository.deleteAllByUser(testUser);
        recommendationRepository.deleteAllByUser(testUser);
        wishlistRepository.deleteAllByUser(testUser);
        testGame = gameRepository.findByName("Hades").orElseThrow(); // cambia si no coincide
    }

    @Test
    public void shouldAddGameToWishlist_WhenGameIsValid() {
        gameService.addGameToWishlist(testGame.getId(), testUser);

        var wishlist = wishlistRepository.findByUserAndGame(testUser, testGame);
        assertTrue(wishlist.isPresent());
    }

    @Test
    public void shouldThrowException_WhenAddingDuplicateToWishlist() {
        gameService.addGameToWishlist(testGame.getId(), testUser);

        assertThrows(RuntimeException.class, () -> {
            gameService.addGameToWishlist(testGame.getId(), testUser);
        });
    }

    @Test
    public void shouldThrowException_WhenAddingNonExistentGameToWishlist() {
        UUID fakeId = UUID.randomUUID();

        assertThrows(RuntimeException.class, () -> {
            gameService.addGameToWishlist(fakeId, testUser);
        });
    }

    @Test
    public void shouldRemoveGameFromWishlist_WhenItExists() {
        gameService.addGameToWishlist(testGame.getId(), testUser);
        gameService.removeGameFromWishlist(testGame.getId(), testUser);

        var wishlist = wishlistRepository.findByUserAndGame(testUser, testGame);
        assertTrue(wishlist.isEmpty());
    }

    @Test
    public void shouldNotFail_WhenRemovingGameNotInWishlist() {
        assertDoesNotThrow(() -> {
            gameService.removeGameFromWishlist(testGame.getId(), testUser);
        });
    }

    @Test
    public void shouldAddGameToLibrary_WhenStoreIsValid() {
        Store steam = storeRepository.findByName("Steam").orElseThrow();
        gameService.addGameToLibraries(testGame.getId(), List.of(steam.getId()), testUser);

        boolean exists = gameUserRepository.existsByLibrary_UserAndGame(testUser, testGame);
        assertTrue(exists);
    }

    @Test
    public void shouldThrowException_WhenAddingGameToInvalidStore() {
        UUID fakeStoreId = UUID.randomUUID();

        assertThrows(RuntimeException.class, () -> {
            gameService.addGameToLibraries(testGame.getId(), List.of(fakeStoreId), testUser);
        });
    }

    @Test
    public void shouldCreateReview_WhenGameIsInLibrary() {
        Store steam = storeRepository.findByName("Steam").orElseThrow();
        gameService.addGameToLibraries(testGame.getId(), List.of(steam.getId()), testUser);
        gameService.reviewGame(testUser, testGame.getId(), true);

        Optional<GameReview> reviewOpt = gameReviewRepository.findByUserAndGame(testUser, testGame);
        assertTrue(reviewOpt.isPresent());
        assertTrue(reviewOpt.get().isRecommended());
    }

    @Test
    public void shouldThrowException_WhenReviewingGameNotInLibrary() {
        assertThrows(RuntimeException.class, () -> {
            gameService.reviewGame(testUser, testGame.getId(), true);
        });
    }

    @Test
    public void shouldAddAchievement_WhenUserOwnsGameAndNotUnlocked() {
        Achievement achievement = achievementRepository.findAll().stream()
                .filter(a -> a.getGame().getId().equals(testGame.getId()))
                .findFirst().orElseThrow();

        Store steam = storeRepository.findByName("Steam").orElseThrow();
        gameService.addGameToLibraries(testGame.getId(), List.of(steam.getId()), testUser);
        gameService.addAchievementToGame(testUser, testGame.getId(), achievement.getId());

        List<AchievementUser> achievementUsers = achievementUserRepository.findAllByUserId(testUser.getId());
        assertEquals(1, achievementUsers.size());
    }

    @Test
    public void shouldThrowException_WhenAddingDuplicateAchievement() {
        Achievement achievement = achievementRepository.findAll().stream()
                .filter(a -> a.getGame().getId().equals(testGame.getId()))
                .findFirst().orElseThrow();

        Store steam = storeRepository.findByName("Steam").orElseThrow();
        gameService.addGameToLibraries(testGame.getId(), List.of(steam.getId()), testUser);
        gameService.addAchievementToGame(testUser, testGame.getId(), achievement.getId());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            gameService.addAchievementToGame(testUser, testGame.getId(), achievement.getId());
        });
        assertEquals("Achievement already unlocked", ex.getMessage());
    }

    @Test
    public void shouldThrowException_WhenUserDoesNotOwnGameForAchievement() {
        Achievement achievement = achievementRepository.findAll().stream()
                .filter(a -> a.getGame().getId().equals(testGame.getId()))
                .findFirst().orElseThrow();

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            gameService.addAchievementToGame(testUser, testGame.getId(), achievement.getId());
        });
        assertEquals("GameUser not found for this user and game", ex.getMessage());
    }

    @Test
    public void shouldThrowException_WhenAchievementDoesNotExist() {
        Store steam = storeRepository.findByName("Steam").orElseThrow();
        gameService.addGameToLibraries(testGame.getId(), List.of(steam.getId()), testUser);

        UUID fakeAchievementId = UUID.randomUUID();

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            gameService.addAchievementToGame(testUser, testGame.getId(), fakeAchievementId);
        });
        assertEquals("Achievement not found", ex.getMessage());
    }

    @Test
    public void shouldThrowException_WhenGameDoesNotExistForAchievement() {
        UUID fakeGameId = UUID.randomUUID();
        UUID achievementId = achievementRepository.findAll().getFirst().getId();

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            gameService.addAchievementToGame(testUser, fakeGameId, achievementId);
        });
        assertEquals("Game not found", ex.getMessage());
    }
}
