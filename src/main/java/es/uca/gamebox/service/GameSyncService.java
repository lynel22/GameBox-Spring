package es.uca.gamebox.service;

import es.uca.gamebox.component.SyncPageTracker;
import es.uca.gamebox.component.client.RawgApiClient;
import es.uca.gamebox.dto.RawgGameDetailDto;
import es.uca.gamebox.dto.RawgGameSummaryDto;
import es.uca.gamebox.dto.RawgGamesResponse;
import es.uca.gamebox.entity.Game;
import es.uca.gamebox.entity.Genre;
import es.uca.gamebox.entity.Platform;
import es.uca.gamebox.repository.GameRepository;
import es.uca.gamebox.repository.GenreRepository;
import es.uca.gamebox.repository.PlatformRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameSyncService {

    private final RawgApiClient rawgApiClient;
    private final GameRepository gameRepository;
    private final GenreRepository genreRepository;
    private final PlatformRepository platformRepository;
    private final SyncPageTracker syncPageTracker;

    @PostConstruct
    public void init() {
        int currentPage = syncPageTracker.getLastSyncedPage();
        int pagesToFetch = 3; // Puedes ajustar este número
        int pageSize = 20;

        for (int i = 0; i < pagesToFetch; i++) {
            syncGames(currentPage + i, pageSize);
            System.out.println("Synced page: " + (currentPage + i));
        }

        syncPageTracker.saveLastSyncedPage(currentPage + pagesToFetch);
    }

    public void syncGames(int page, int pageSize) {
        RawgGamesResponse response = rawgApiClient.getGames(page, pageSize);
        if (response == null || response.getResults() == null) return;

        for (RawgGameSummaryDto summary : response.getResults()) {
            Optional<Game> existingGame = gameRepository.findByRawgSlug(summary.getSlug());
            if (existingGame.isPresent()) continue;

            RawgGameDetailDto detail = rawgApiClient.getGameDetails(summary.getSlug());
            if (detail == null) continue;

            Game game = new Game();
            game.setName(detail.getName());
            game.setDescription(stripHtml(detail.getDescription()));
            game.setImageUrl(detail.getBackground_image());
            log.info("Título: {}, Released: {}", detail.getName(), detail.getReleased());
            game.setReleaseDate(detail.getReleased());
            game.setSteamAppId(extractSteamAppId(detail));
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
        }
    }

    private String stripHtml(String html) {
        return html != null ? html.replaceAll("<[^>]*>", "").trim() : "";
    }

    private String extractSteamAppId(RawgGameDetailDto detail) {
        if (detail.getStores() == null) return null;
        return detail.getStores().stream()
                .filter(store -> store.getStore().getSlug().equals("steam"))
                .map(store -> {
                    String url = store.getUrl();
                    try {
                        String[] parts = url.split("app/");
                        if (parts.length > 1) {
                            return parts[1].split("/")[0];
                        }
                    } catch (Exception ignored) {}
                    return null;
                })
                .filter(id -> id != null && !id.isEmpty())
                .findFirst()
                .orElse(null);
    }
}
