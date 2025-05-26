package es.uca.gamebox.dto;

import lombok.Data;

import java.util.List;

@Data
public class RawgGameDetailDto {
    private String slug;
    private String name;
    private String description;
    private String released;
    private String background_image;
    private List<RawgPlatformDto> platforms;
    private List<RawgGenreDto> genres;

    // Getters and setters
}
