package es.uca.gamebox.service;

import es.uca.gamebox.component.SyncPageTracker;
import es.uca.gamebox.component.client.RawgApiClient;
import es.uca.gamebox.dto.rawg.*;
import es.uca.gamebox.entity.*;
import es.uca.gamebox.repository.*;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameSyncService {

    private final RawgApiClient rawgApiClient;
    private final GameRepository gameRepository;
    private final GenreRepository genreRepository;
    private final DeveloperRepository developerRepository;
    private final PlatformRepository platformRepository;
    private final StoreRepository storeRepository;
    private final AchievementRepository achievementRepository;
    private final SyncPageTracker syncPageTracker;

    /*@PostConstruct*/
    public void init() {
        int currentPage = syncPageTracker.getLastSyncedPage();
        int pagesToFetch = 4; // Puedes ajustar este número
        int pageSize = 40;

        for (int i = 0; i < pagesToFetch; i++) {
            syncGames(currentPage + i, pageSize);
            System.out.println("Synced page: " + (currentPage + i));
        }

        syncPageTracker.saveLastSyncedPage(currentPage + pagesToFetch);
    }

    @Transactional
    public void syncGames(int page, int pageSize) {
        RawgGamesResponse response = rawgApiClient.getGames(page, pageSize);
        if (response == null || response.getResults() == null) return;

        for (RawgGameSummaryDto summary : response.getResults()) {
            Optional<Game> existingGame = gameRepository.findByRawgSlug(summary.getSlug());
            if (existingGame.isPresent()) continue;

            RawgGameDetailDto detail = rawgApiClient.getGameDetails(summary.getSlug());
            if (detail == null) continue;

            // Obtener las stores asociadas al juego
            RawgStoresResponseDto storesResponse = rawgApiClient.getGameStores(summary.getSlug());
            List<RawgGameStoreEntryDto> stores = storesResponse != null ? storesResponse.getResults() : List.of();

            Game game = new Game();
            game.setName(detail.getName());
            game.setDescription(stripHtml(detail.getDescription()));
            game.setImageUrl(detail.getBackground_image());
            game.setReleaseDate(detail.getReleased());
            game.setSteamAppId(extractSteamAppId(stores));
            game.setRawgSlug(detail.getSlug());
            game.setCreatedAt(LocalDateTime.now());
            game.setUpdatedAt(LocalDateTime.now());

            // Sincronizar géneros
            if (detail.getGenres() != null) {
                List<Genre> genres = detail.getGenres().stream()
                        .map(rawgGenre -> {
                            try {
                                return genreRepository.findByNameIgnoreCase(rawgGenre.getName())
                                        .orElseGet(() -> genreRepository.save(new Genre(rawgGenre.getName())));
                            } catch (Exception e) {
                                log.warn("No se pudo guardar el género '{}': {}", rawgGenre.getName(), e.getMessage());
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                game.setGenres(genres);
            }

            // Sincronizar desarrollador
            if (detail.getDevelopers() != null && !detail.getDevelopers().isEmpty()) {
                String developerName = detail.getDevelopers().get(0).getName();
                Developer developer = developerRepository.findByNameIgnoreCase(developerName)
                        .orElseGet(() -> developerRepository.save(new Developer(developerName)));
                game.setDeveloper(developer);
            }

            // Sincronizar plataformas
            if (detail.getPlatforms() != null) {
                List<Platform> platforms = detail.getPlatforms().stream()
                        .map(rawgPlatform -> {
                            try {
                                String platformName = rawgPlatform.getPlatform().getName();
                                return platformRepository.findByNameIgnoreCase(platformName)
                                        .orElseGet(() -> platformRepository.save(new Platform(platformName)));
                            } catch (Exception e) {
                                log.warn("No se pudo guardar la plataforma: {}", e.getMessage());
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                game.setPlatforms(platforms);
            }

            gameRepository.save(game);

            associateStoresWithGame(game, stores);

            // Sincronizar logros
            RawgAchievementResponse achievementResponse = rawgApiClient.getAchievementsForGame(detail.getSlug());
            if (achievementResponse != null && achievementResponse.getResults() != null) {
                for (RawgAchievementDto rawgAchievement : achievementResponse.getResults()) {
                    Achievement achievement = new Achievement();
                    achievement.setGame(game);
                    achievement.setName(rawgAchievement.getName());
                    achievement.setDescription(rawgAchievement.getDescription());
                    achievement.setImageUrl(rawgAchievement.getImage());
                    achievement.setCreatedAt(LocalDateTime.now());
                    achievement.setUpdatedAt(LocalDateTime.now());
                    achievementRepository.save(achievement);
                }
            }
        }
    }

    public String extractSteamAppId(List<RawgGameStoreEntryDto> stores) {
        if (stores == null || stores.isEmpty()) {
            System.out.println("No stores found for the game.");
            return null;
        }
        for (RawgGameStoreEntryDto entry : stores) {
            String url = entry.getUrl();
            if (url != null && url.contains("store.steampowered.com")) {
                Pattern pattern = Pattern.compile("store\\.steampowered\\.com/app/(\\d+)");
                Matcher matcher = pattern.matcher(url);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        }
        return null;
    }

    private void associateStoresWithGame(Game game, List<RawgGameStoreEntryDto> storeEntries) {
        Set<Store> associatedStores = new HashSet<>();

        for (RawgGameStoreEntryDto entry : storeEntries) {
            try {
                int rawgStoreId = Integer.parseInt(entry.getStoreId());
                Optional<Store> optionalStore = storeRepository.findByRawgId(rawgStoreId);
                optionalStore.ifPresent(associatedStores::add);
            } catch (Exception e) {
                log.warn("No se pudo asociar store con id '{}': {}", entry.getStoreId(), e.getMessage());
            }
        }

        game.setStores(associatedStores);
    }


    private String stripHtml(String html) {
        return html != null ? html.replaceAll("<[^>]*>", "").trim() : "";
    }

    @Transactional
    public void updateGamesWithoutStores() {
        List<Game> gamesWithoutStores = gameRepository.findAll().stream()
                .filter(game -> game.getStores() == null || game.getStores().isEmpty())
                .collect(Collectors.toList());

        for (Game game : gamesWithoutStores) {
            String slug = game.getRawgSlug();
            if (slug == null || slug.isBlank()) continue;

            RawgStoresResponseDto storesResponse = rawgApiClient.getGameStores(slug);
            if (storesResponse == null || storesResponse.getResults() == null) continue;

            int originalStoreCount = game.getStores().size();
            associateStoresWithGame(game, storesResponse.getResults());

            if (game.getStores().size() > originalStoreCount) {
                game.setUpdatedAt(LocalDateTime.now());
                gameRepository.save(game);
                log.info("Actualizadas tiendas para el juego '{}'", game.getName());
            }
        }
    }

   /* public String extractSteamAppId(String slug) {
        RawgStoresResponseDto storesResponse = rawgApiClient.getGameStores(slug);

        if (storesResponse == null || storesResponse.getResults() == null) return null;

        for (RawgGameStoreEntryDto entry : storesResponse.getResults()) {
            String url = entry.getUrl();
            if (url != null && url.contains("store.steampowered.com")) {
                Pattern pattern = Pattern.compile("store\\.steampowered\\.com/app/(\\d+)");
                Matcher matcher = pattern.matcher(url);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        }

        return null;
    }*/
}
