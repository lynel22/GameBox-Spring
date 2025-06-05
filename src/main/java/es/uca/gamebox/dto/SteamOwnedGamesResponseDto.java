package es.uca.gamebox.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
public class SteamOwnedGamesResponseDto {
    private Response response;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private List<Game> games;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Game {
        private int appid;
        private String name;
        private int playtime_forever; // en minutos
        private long rtime_last_played; // epoch time en segundos
        private String img_icon_url;
    }
}
