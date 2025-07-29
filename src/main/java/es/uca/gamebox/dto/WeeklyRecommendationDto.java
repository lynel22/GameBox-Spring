package es.uca.gamebox.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class WeeklyRecommendationDto {
    private UUID gameId;
    private String title;
    private String imageUrl;
    private String releaseDate;
    private GenreDto genreRecommendedBy;
    private List<GenreDto> genres;
    private List<StoreDto> stores;
}
