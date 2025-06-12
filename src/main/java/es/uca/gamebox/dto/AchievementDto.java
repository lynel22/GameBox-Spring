package es.uca.gamebox.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AchievementDto {
    private UUID id;
    private String name;
    private String description;
    private String imageUrl;
    private LocalDateTime dateUnlocked;
    private boolean unlocked;
}
