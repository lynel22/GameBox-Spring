package es.uca.gamebox.controller;

import es.uca.gamebox.dto.*;
import es.uca.gamebox.dto.request.StoreIdsRequest;
import es.uca.gamebox.entity.User;
import es.uca.gamebox.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = {"/game"})
public class GameController {
    @Autowired
    private final GameService gameService;

    @GetMapping("/library")
    public List<GameDto> getLibrary(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthorized access attempt to game library");
            throw new RuntimeException("Unauthorized access");
        }
        log.info("Fetching game library for user: {}", authentication.getName());
        User user = (User) authentication.getPrincipal();
        return gameService.getLibrary(user);
    }

    @GetMapping("/library-by-store")
    public List<GameDto> getLibraryByStore(
            @RequestParam("store") String storeName,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthorized access attempt to {} game library", storeName);
            throw new RuntimeException("Unauthorized access");
        }

        User user = (User) authentication.getPrincipal();
        return gameService.getGamesByStoreName(user, storeName);
    }


    @GetMapping("/detail")
    public GameDetailDto getGameDetail(Authentication authentication, @RequestParam UUID gameId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthorized access attempt to game detail");
            throw new RuntimeException("Unauthorized access");
        }

        User user = (User) authentication.getPrincipal();

        System.out.println("Fetching game detail for user: " + user.getUsername() + " and gameId: " + gameId);
        return gameService.getGameDetail(user, gameId);
    }

    @PostMapping("/add-achievement")
    public ResponseEntity<String> addAchievement(Authentication authentication, @RequestParam UUID gameId, @RequestParam UUID achievementId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthorized access attempt to add achievement");
            throw new RuntimeException("Unauthorized access");
        }

        User user = (User) authentication.getPrincipal();
        gameService.addAchievementToGame(user, gameId, achievementId);

        log.info("Achievement {} added to game {} for user {}", achievementId, gameId, user.getUsername());
        return  ResponseEntity.ok("Achievement added successfully");
    }

    @GetMapping("/search")
    public List<GameDto> searchGames(@RequestParam String q) {
        if (q == null || q.trim().isEmpty()) {
            return List.of();
        }
        System.out.println("Searching games with query: " + q);
        return gameService.searchGamesByName(q.trim());
    }

    @PostMapping("/add-game-to-libraries")
    public ResponseEntity<String> addGameToLibraries(
            Authentication authentication,
            @RequestParam UUID gameId,
            @RequestBody StoreIdsRequest request
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthorized access attempt to add game to libraries");
            throw new RuntimeException("Unauthorized access");
        }

        if (request.getStoreIds() == null || request.getStoreIds().isEmpty()) {
            return ResponseEntity.badRequest().body("Empty storeIDs list.");
        }

        User user = (User) authentication.getPrincipal();
        gameService.addGameToLibraries(gameId, request.getStoreIds(), user);
        return ResponseEntity.ok("Game added to libraries successfully");
    }

    @GetMapping("/library-game-count")
    public List<LibraryGameCountDto> getLibraryGameCount(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthorized access attempt to library game count");
            throw new RuntimeException("Unauthorized access");
        }
        User user = (User) authentication.getPrincipal();
        return gameService.getLibraryGameCount(user);
    }

    @PostMapping("/add-game-to-wishlist")
    public ResponseEntity<String> addGameToWishlist(
            Authentication authentication,
            @RequestParam UUID gameId
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthorized access attempt to add game to wishlist");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        User user = (User) authentication.getPrincipal();
        gameService.addGameToWishlist(gameId, user);
        return ResponseEntity.ok("Game added to wishlist successfully");
    }

    @DeleteMapping("/remove-game-from-wishlist")
    public ResponseEntity<String> removeGameFromWishlist(
            Authentication authentication,
            @RequestParam UUID gameId
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthorized access attempt to remove game from wishlist");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        User user = (User) authentication.getPrincipal();
        gameService.removeGameFromWishlist(gameId, user);
        return ResponseEntity.ok("Game removed from wishlist successfully");
    }

    @GetMapping("/user-wishlist")
    public List<GameWishlistDto> getUserWishlist(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthorized access attempt to user wishlist");
            throw new RuntimeException("Unauthorized access");
        }
        User user = (User) authentication.getPrincipal();
        return gameService.getUserWishlist(user);
    }

    @GetMapping("/deals")
    public List<DealDto> getDeals(
            @RequestParam(required = false) String store,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthorized access attempt to deals");
            throw new RuntimeException("Unauthorized access");
        }

        User user = (User) authentication.getPrincipal();
        return gameService.getDeals(user, store);
    }

    @PostMapping("/review")
    public void reviewGame(Authentication authentication,
                           @RequestBody Map<String, Object> request) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthorized access attempt to review game");
            throw new RuntimeException("Unauthorized access");
        }
        User user = (User) authentication.getPrincipal();
        UUID gameId = UUID.fromString(request.get("gameId").toString());
        boolean recommended = Boolean.parseBoolean(request.get("recommended").toString());
        gameService.reviewGame(user, gameId, recommended);
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<WeeklyRecommendationDto>> getRecommendationsForUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthorized access attempt to weekly recommendations");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User currentUser = (User) authentication.getPrincipal();
        List<WeeklyRecommendationDto> recommendations = gameService.getWeeklyRecommendationsForUser(currentUser);
        return ResponseEntity.ok(recommendations);
    }

}
