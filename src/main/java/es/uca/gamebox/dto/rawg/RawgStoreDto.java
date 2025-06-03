package es.uca.gamebox.dto.rawg;

import lombok.Data;

@Data
public class RawgStoreDto {
    private int id;
    private String name;
    private String domain;
    private String slug;
    private String image_background;
}
