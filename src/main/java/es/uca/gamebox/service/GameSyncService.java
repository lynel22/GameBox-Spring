package es.uca.gamebox.service;

import es.uca.gamebox.client.RawgApiClient;
import es.uca.gamebox.dto.RawgGameDetailDto;
import es.uca.gamebox.dto.RawgGameSummaryDto;
import es.uca.gamebox.dto.RawgGamesResponse;
import es.uca.gamebox.entity.Game;
import es.uca.gamebox.entity.Genre;
import es.uca.gamebox.repository.GameRepository;
import es.uca.gamebox.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameSyncService {

    private final RawgApiClient rawgApiClient;
    private final GameRepository gameRepository;
    private final GenreRepository genreRepository;

    public void syncGames(int page, int pageSize) {
        RawgGamesResponse response = rawgApiClient.getGames(page, pageSize);
        if (response == null || response.getResults() == null) return;

        for (RawgGameSummaryDto summary : response.getResults()) {
            Optional<Game> existingGame = gameRepository.findByRawgSlug(summary.getSlug());
            if (existingGame.isPresent()) continue; // Ya sincronizado

            RawgGameDetailDto detail = rawgApiClient.getGameDetails(summary.getSlug());
            if (detail == null) continue;

            Game game = new Game();
            game.setName(detail.getName());
            game.setDescription(stripHtml(detail.getDescription()));
            game.setImageUrl(detail.getBackground_image());
            game.setReleaseDate(detail.getReleased());
            game.setSteamAppId(extractSteamAppId(detail));
            game.setRawgSlug(detail.getSlug());
            game.setCreatedAt(LocalDateTime.now());
            game.setUpdatedAt(LocalDateTime.now());

            List<Genre> genres = detail.getGenres().stream()
                    .map(rawgGenre -> genreRepository.findByNameIgnoreCase(rawgGenre.getName())
                            .orElseGet(() -> genreRepository.save(new Genre(rawgGenre.getName()))))
                    .collect(Collectors.toList());
            game.setGenres(genres);

            gameRepository.save(game);
        }
    }

    private String stripHtml(String html) {
        return html.replaceAll("<[^>]*>", "").trim();
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
