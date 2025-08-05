package es.uca.gamebox.service;

import es.uca.gamebox.dto.*;
import es.uca.gamebox.entity.*;

import es.uca.gamebox.mapper.GameMapper;
import es.uca.gamebox.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameUserRepository gameUserRepository;
    private final GameRepository gameRepository;
    private final AchievementRepository achievementRepository;
    private final UserRepository userRepository;
    private final AchievementUserRepository achievementUserRepository;
    private final LibraryRepository libraryRepository;
    private final StoreRepository storeRepository;
    private final WishlistRepository wishlistRepository;
    private final DealRepository dealRepository;
    private final GameReviewRepository gameReviewRepository;
    private final RecommendationRepository recommendationRepository;

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

        boolean inWishlist = wishlistRepository.existsByUserAndGame(managedUser, game);

        // Reviews
        long positive = gameReviewRepository.countByGameAndRecommended(game, true);
        long negative = gameReviewRepository.countByGameAndRecommended(game, false);
        long total = positive + negative;
        double percentage = total > 0 ? (positive * 100.0 / total) : 0.0;

        Optional<GameReview> userReviewOpt = gameReviewRepository.findByUserAndGame(managedUser, game);
        Boolean userReview = userReviewOpt.map(GameReview::isRecommended).orElse(null);
        String summaryText = getReviewSummaryText(percentage);

        return GameMapper.toGameDetailDto(
                game,
                achievements,
                unlockedAchievements,
                friendsWithGame,
                gameUser,
                inWishlist,
                (int) positive,
                (int) negative,
                percentage,
                summaryText,
                userReview
        );


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

        wishlistRepository.findByUserAndGame(user, game)
                .ifPresent(wishlistRepository::delete);
    }



    public List<GameWishlistDto> getUserWishlist(User user) {
        List<Wishlist> wishlistEntries = wishlistRepository.findByUser(user);

        return wishlistEntries.stream()
                .map(GameMapper::toGameWishlistDto)
                .collect(Collectors.toList());
    }

    public List<DealDto> getDeals(User user, String store) {
        List<Deal> activeDeals;

        if (store != null && !store.isBlank()) {
            activeDeals = dealRepository.findByEndDateIsNullAndStore_NameIgnoreCase(store.trim());
        } else {
            activeDeals = dealRepository.findByEndDateIsNull();
        }

        return activeDeals.stream()
                .map(deal -> new DealDto(
                        deal.getGame().getName(),
                        deal.getGame().getId().toString(),
                        deal.getGame().getImageUrl(),
                        deal.getNormalPrice(),
                        deal.getSalePrice(),
                        deal.getSavings(),
                        deal.getStore() != null ? deal.getStore().getName() : "Desconocido",
                        deal.getDealUrl()
                ))
                .toList();

    }

    @Transactional
    public void reviewGame(User user, UUID gameId, boolean recommended) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        boolean hasGameInLibrary = gameUserRepository.existsByLibrary_UserAndGame(user, game);
        if (!hasGameInLibrary) {
            throw new IllegalStateException("Cannot review a game not in your library");
        }

        Optional<GameReview> existingReview = gameReviewRepository.findByUserAndGame(user, game);
        if (existingReview.isPresent()) {
            GameReview review = existingReview.get();
            review.setRecommended(recommended);
            gameReviewRepository.save(review);
        } else {
            GameReview review = new GameReview();
            review.setUser(user);
            review.setGame(game);
            review.setRecommended(recommended);
            gameReviewRepository.save(review);
        }
    }

    private String getReviewSummaryText(double percentage) {
        if (percentage >= 95) return "Extremadamente positivas";
        else if (percentage >= 90) return "Muy positivas";
        else if (percentage >= 80) return "Positivas";
        else if (percentage >= 75) return "Mayoritariamente positivas";
        else if (percentage >= 60) return "Opiniones mixtas";
        else if (percentage > 0) return "Pocas recomendaciones";
        else return "Sin valoraciones";
    }

    public List<WeeklyRecommendationDto> getWeeklyRecommendationsForUser(User user) {
        LocalDate today = LocalDate.now();
        return recommendationRepository.findByUserAndRecommendationDate(user, today).stream()
                .map(rec -> {
                    Game game = rec.getGame();

                    WeeklyRecommendationDto dto = new WeeklyRecommendationDto();
                    dto.setGameId(game.getId());
                    dto.setTitle(game.getName());
                    dto.setImageUrl(game.getImageUrl());
                    dto.setReleaseDate(game.getReleaseDate());

                    // Género por el que se recomienda
                    GenreDto reasonGenre = new GenreDto();
                    reasonGenre.setId(rec.getGenre().getId());
                    reasonGenre.setName(rec.getGenre().getName());
                    reasonGenre.setSpanishName(rec.getGenre().getSpanishName());
                    dto.setGenreRecommendedBy(reasonGenre);

                    // Géneros del juego
                    List<GenreDto> genreDtos = game.getGenres().stream().map(g -> {
                        GenreDto gd = new GenreDto();
                        gd.setId(g.getId());
                        gd.setName(g.getName());
                        gd.setSpanishName(g.getSpanishName());
                        return gd;
                    }).toList();
                    dto.setGenres(genreDtos);

                    // Stores
                    List<StoreDto> storeDtos = game.getStores().stream().map(store -> {
                        StoreDto sd = new StoreDto();
                        sd.setId(store.getId());
                        sd.setName(store.getName());
                        sd.setImageUrl(store.getImageUrl());
                        return sd;
                    }).toList();
                    dto.setStores(storeDtos);

                    return dto;
                }).toList();
    }

}
