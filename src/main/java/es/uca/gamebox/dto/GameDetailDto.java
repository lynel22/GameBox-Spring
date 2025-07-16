package es.uca.gamebox.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class GameDetailDto {
    private UUID id;
    private String name;
    private String description;
    private String imageUrl;
    private String releaseDate;

    private DeveloperDto developer;
    private List<GenreDto> genres;
    private List<AchievementDto> achievements;
    private List<FriendDto> friendsThatOwnIt;
    private List<StoreDto> stores;

    private LocalDateTime lastPlayed;
    private Float hoursPlayed;

    private boolean inWishlist;
    private boolean ownedByUser;

    private List<DealDto> deals;
}
