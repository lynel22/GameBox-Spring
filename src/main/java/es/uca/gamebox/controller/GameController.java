package es.uca.gamebox.controller;

import es.uca.gamebox.dto.GameDetailDto;
import es.uca.gamebox.dto.GameDto;
import es.uca.gamebox.entity.Game;
import es.uca.gamebox.entity.User;
import es.uca.gamebox.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

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

    @GetMapping("/library/steam")
    public List<GameDto> getSteamLibrary(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthorized access attempt to steam game library");
            throw new RuntimeException("Unauthorized access");
        }
        User user = (User) authentication.getPrincipal();
        return gameService.getGamesByStoreName(user, "Steam");
    }

    @GetMapping("/library/epic")
    public List<GameDto> getEpicLibrary(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthorized access attempt to epic game library");
            throw new RuntimeException("Unauthorized access");
        }
        User user = (User) authentication.getPrincipal();
        return gameService.getGamesByStoreName(user, "Epic Games");
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
    public ResponseEntity<?> addGameToLibraries(Authentication authentication,
                                                @RequestParam UUID gameId,
                                                @RequestParam List<UUID> storeIds) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthorized access attempt to add game to libraries");
            throw new RuntimeException("Unauthorized access");
        }
        if (storeIds == null || storeIds.isEmpty()) {
            return ResponseEntity.badRequest().body("Empty storeIDs list.");
        }

        User user = (User) authentication.getPrincipal();
        gameService.addGameToLibraries(gameId, storeIds, user);
        return ResponseEntity.ok("Game added to libraries successfully");
    }

}
