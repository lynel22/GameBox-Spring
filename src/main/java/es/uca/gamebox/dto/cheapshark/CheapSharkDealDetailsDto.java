package es.uca.gamebox.dto.cheapshark;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CheapSharkDealDetailsDto {

    @JsonProperty("dealID")
    private String dealID;

    @JsonProperty("gameInfo")
    private GameInfo gameInfo;

    @Data
    public static class GameInfo {

        @JsonProperty("storeID")
        private String storeID;

        @JsonProperty("steamAppID")
        private String steamAppID;

        @JsonProperty("title")
        private String title;

        @JsonProperty("dealID")
        private String dealID;

        @JsonProperty("thumb")
        private String thumb;

        @JsonProperty("gameID")
        private String gameID;

        @JsonProperty("metacriticLink")
        private String metacriticLink;

        @JsonProperty("steamRatingUrl")
        private String steamRatingUrl;

        @JsonProperty("dealLink")
        private String dealLink;
    }
}
