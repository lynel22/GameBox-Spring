package es.uca.gamebox.dto;

import lombok.Data;

@Data
public class SteamPlayerAchievementDto {
    private String apiname;
    private String name;         // Display name
    private String description;
    private int achieved;        // 1 si está desbloqueado, 0 si no
    private Long unlocktime;     // Unix timestamp
}
