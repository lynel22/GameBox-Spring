package es.uca.gamebox.dto.steam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SteamPlayerAchievementsResponseDto {
    private PlayerStats playerstats;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlayerStats {
        private List<SteamPlayerAchievementDto> achievements;
    }
}
