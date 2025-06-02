package es.uca.gamebox.dto.rawg;

import lombok.Data;

@Data
public class RawgGameSummaryDto {
    private String slug;
    private String name;
    private String released;
    private String background_image;
}
