package es.uca.gamebox.service;

import es.uca.gamebox.component.client.SteamApiClient;
import es.uca.gamebox.dto.steam.SteamGameSchemaResponseDto;
import es.uca.gamebox.entity.Achievement;
import es.uca.gamebox.entity.Game;
import es.uca.gamebox.repository.AchievementRepository;
import es.uca.gamebox.repository.GameRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class SteamAchievementImportService {

    private final SteamApiClient steamApiClient;
    private final GameRepository gameRepository;
    private final AchievementRepository achievementRepository;

    /*@PostConstruct*/
    public void init() {
        new Thread(this::importSteamAchievementsForAllGames).start();
    }

    @Transactional
    public void importSteamAchievementsForAllGames() {
        List<Game> games = gameRepository.findBySteamAppIdNotNullAndSteamAchievementsSyncedFalse();

        for (Game game : games) {
            try {
                processGameAchievements(game);
                Thread.sleep(1000); // Espera entre juegos
            } catch (Exception e) {
                handleException(e, game);
            }
        }
    }

    private void processGameAchievements(Game game) {
        System.out.println("Syncing achievements for game: " + game.getName() + " from Steam");
        Long appId = Long.valueOf(game.getSteamAppId());
        List<SteamGameSchemaResponseDto.Achievement> achievements = steamApiClient.getAllAchievementsForGame(appId);

        if (achievements == null || achievements.isEmpty()) {
            System.out.println("No achievements found for game: " + game.getName());
            markGameAsSynced(game);
            return;
        }

        saveNewAchievements(game, achievements);
        markGameAsSynced(game);
    }

    private void saveNewAchievements(Game game, List<SteamGameSchemaResponseDto.Achievement> achievements) {
        for (SteamGameSchemaResponseDto.Achievement a : achievements) {
            if (!achievementRepository.existsByGameAndName(game, a.getDisplayName())) {
                System.out.println("Adding achievement: " + a.getDisplayName() + " for game: " + game.getName());
                Achievement newAchievement = new Achievement();
                newAchievement.setGame(game);
                newAchievement.setName(a.getDisplayName());
                newAchievement.setDescription(
                        a.getDescription() != null ? a.getDescription() : "No description"
                );
                newAchievement.setImageUrl(a.getIcon()); // Usa icongray si prefieres el icono gris
                achievementRepository.save(newAchievement);
            }
        }
    }

    private void markGameAsSynced(Game game) {
        game.setSteamAchievementsSynced(true);
        gameRepository.save(game);
    }

    private void handleException(Exception e, Game game) {
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        System.err.println("Error syncing achievements for game " + game.getName() + ": " + e.getMessage());
    }
}
