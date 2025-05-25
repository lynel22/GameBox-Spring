package es.uca.gamebox.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
public class SteamOwnedGamesResponseDTO {
    private Response response;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private List<Game> games;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Game {
        private String appid;
        private String name;
        private float playtime_forever;
        private String img_icon_url;
    }
}
