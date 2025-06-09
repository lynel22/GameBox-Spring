package es.uca.gamebox.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AchievementDto {
    private UUID id;
    private String name;
    private String description;
    private String imageUrl;
    private boolean unlocked;
}
