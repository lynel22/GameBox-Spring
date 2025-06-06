package es.uca.gamebox.dto.steam;

import lombok.Data;

import java.util.List;

@Data
public class SteamGameSchemaResponseDto {
    private Game game;

    @Data
    public static class Game {
        private AvailableGameStats availableGameStats;
    }

    @Data
    public static class AvailableGameStats {
        private List<Achievement> achievements;
    }

    @Data
    public static class Achievement {
        private String name;
        private String displayName;
        private String description;
        private String icon;
        private String icongray;
    }
}
