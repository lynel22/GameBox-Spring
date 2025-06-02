package es.uca.gamebox.dto.rawg;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RawgGameStoreEntryDto {
    private int id;
    @JsonProperty("game_id")
    private String gameId;
    @JsonProperty("store_id")
    private String storeId;
    private String url;
}
