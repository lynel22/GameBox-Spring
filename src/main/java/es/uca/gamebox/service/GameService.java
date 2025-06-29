package es.uca.gamebox.service;

import es.uca.gamebox.dto.GameDetailDto;
import es.uca.gamebox.dto.GameDto;
import es.uca.gamebox.dto.LibraryGameCountDto;
import es.uca.gamebox.entity.*;

import es.uca.gamebox.mapper.GameMapper;
import es.uca.gamebox.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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

    @Autowired
    LibraryRepository libraryRepository;

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    WishlistRepository wishlistRepository;

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

        // Buscar todas las instancias del juego en bibliotecas del usuario
        List<GameUser> gameUsers = gameUserRepository.findAllByLibraryUserIdAndGameId(user.getId(), game.getId());

        if (gameUsers.isEmpty()) {
            throw new RuntimeException("GameUser not found for this user and game");
        }

        GameUser gameUser = gameUsers.getFirst();

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

    public void addGameToLibraries(UUID gameId, List<UUID> storeIds, User user) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Juego no encontrado."));

        // Buscar y eliminar si el juego estaba en la wishlist del usuario
        wishlistRepository.findByUserAndGame(user, game)
                .ifPresent(wishlistRepository::delete);

        for (UUID storeId : storeIds) {
            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new RuntimeException("Store no encontrada: " + storeId));

            // Buscar o crear la biblioteca del usuario para esta store
            Library library = libraryRepository.findByUserAndStore(user, store)
                    .orElseGet(() -> {
                        Library newLibrary = new Library();
                        newLibrary.setStore(store);
                        newLibrary.setUser(user);
                        newLibrary.setName("Biblioteca de " + store.getName());
                        newLibrary.setCreatedAt(new Date());
                        newLibrary.setUpdatedAt(LocalDateTime.now());
                        return libraryRepository.save(newLibrary);
                    });

            // Verificar si ya tiene el juego
            boolean exists = gameUserRepository.existsByLibraryAndGame(library, game);
            if (!exists) {
                GameUser gameUser = new GameUser();
                gameUser.setLibrary(library);
                gameUser.setGame(game);
                gameUser.setHoursPlayed(0f);
                gameUser.setSynced(false);
                gameUser.setCreatedAt(LocalDateTime.now());
                gameUser.setUpdatedAt(LocalDateTime.now());

                gameUserRepository.save(gameUser);
            }
        }
    }


    public List<LibraryGameCountDto> getLibraryGameCount(User user) {
        return gameUserRepository.countGamesGroupedByStore(user);
    }

    public void addGameToWishlist(UUID gameId, User user) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        if (wishlistRepository.existsByUserAndGame(user, game)) {
            throw new RuntimeException("Game already in wishlist");
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setGame(game);

        wishlistRepository.save(wishlist);
    }

    public void removeGameFromWishlist(UUID gameId, User user) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        Wishlist wishlist = wishlistRepository.findByUserAndGame(user, game)
                .orElseThrow(() -> new RuntimeException("Game not found in wishlist"));

        wishlistRepository.delete(wishlist);
    }

}
