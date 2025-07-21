package es.uca.gamebox.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class GameSummaryDto {
    private UUID id;
    private String name;
    private String imageUrl;
    private String storeName;
    private String storeImageUrl;
    private double hoursPlayed;
    private LocalDateTime lastSession;
    private int achievementsUnlocked;
    private int totalAchievements;
}
