package es.uca.gamebox.service;

import es.uca.gamebox.dto.GameDetailDto;
import es.uca.gamebox.dto.GameDto;
import es.uca.gamebox.entity.*;

import es.uca.gamebox.mapper.GameMapper;
import es.uca.gamebox.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class GameService {

    @Autowired
    GameUserRepository gameUserRepository;

    @Autowired
    GameRepository gameRepository;

    @Autowired
    AchievementRepository achievementRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AchievementUserRepository achievementUserRepository;

    public List<GameDto> getLibrary(User currentUser) {
        List<Game> games = gameUserRepository.findGamesByUser(currentUser);
        return GameMapper.toDtoList(games);
    }

    public List<GameDto> getGamesByStoreName(User user, String storeName) {
        List<Game> games = gameUserRepository.findGamesByUserAndStoreName(user, storeName);
        return GameMapper.toDtoList(games);
    }

    @Transactional
    public GameDetailDto getGameDetail(User user, UUID gameId) {
        // This ensures the 'user' object is managed by the current session.
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Game game = gameRepository.findWithDetailsById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        List<Achievement> achievements = achievementRepository.findAllByGame(game);

        List<AchievementUser> unlockedAchievements =
                achievementUserRepository.findAllByGameUser_GameAndUser(game, managedUser);

        List<User> friendsWithGame = managedUser.getFriends().stream()
                .filter(friend -> friend.getLibraries().stream()
                        .flatMap(library -> library.getGameUsers().stream())
                        .anyMatch(gameUser -> gameUser.getGame().equals(game)))
                .toList();

        GameUser gameUser = managedUser.getLibraries().stream()
                .flatMap(library -> library.getGameUsers().stream())
                .filter(gu -> gu.getGame().equals(game))
                .findFirst()
                .orElse(null);


        return GameMapper.toGameDetailDto(game, achievements, unlockedAchievements, friendsWithGame, gameUser);

    }

    public void addAchievementToGame(User user, UUID gameId, UUID achievementId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new RuntimeException("Achievement not found"));

        // Buscar el GameUser correspondiente
        GameUser gameUser = gameUserRepository.findByUserAndGame(user.getId(), game.getId())
                .orElseThrow(() -> new RuntimeException("GameUser not found for this user and game"));

        // Verificar si el usuario ya ha desbloqueado el logro
        boolean alreadyUnlocked = achievementUserRepository.existsByUserAndAchievement(user, achievement);
        if (alreadyUnlocked) {
            throw new RuntimeException("Achievement already unlocked");
        }

        // Crear el AchievementUser
        AchievementUser achievementUser = new AchievementUser();
        achievementUser.setAchievement(achievement);
        achievementUser.setUser(user);
        achievementUser.setGameUser(gameUser);
        achievementUser.setDateUnlocked(LocalDateTime.now());

        achievementUserRepository.save(achievementUser);
    }

    public List<GameDto> searchGamesByName(String query) {
        List<Game> games = gameRepository.findTop20ByNameContainingIgnoreCase(query);
        return GameMapper.toDtoList(games);
    }

}
