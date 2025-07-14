package es.uca.gamebox.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class GameWishlistDto {
    private UUID id;
    private String name;
    private String imageUrl;
    private String releaseDate;

    private List<GenreDto> genres;
    private List<StoreDto> stores;

    private LocalDateTime fechaAdicion;
    private List<DealDto> deals;
}
