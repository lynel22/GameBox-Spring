package es.uca.gamebox.controller;

import es.uca.gamebox.dto.GameDetailDto;
import es.uca.gamebox.dto.GameDto;
import es.uca.gamebox.dto.GameWishlistDto;
import es.uca.gamebox.dto.LibraryGameCountDto;
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
    public ResponseEntity<?> addAchievement(Authentication authentication, @RequestParam UUID gameId, @RequestParam UUID achievementId) {
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
    public ResponseEntity<?> addGameToLibraries(
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
    public ResponseEntity<?> addGameToWishlist(
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
    public ResponseEntity<?> removeGameFromWishlist(
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


}
