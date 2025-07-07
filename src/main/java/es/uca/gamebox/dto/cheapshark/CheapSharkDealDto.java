package es.uca.gamebox.dto.cheapshark;

import lombok.Data;

@Data
public class CheapSharkDealDto {
    private String internalName;
    private String title;
    private String metacriticLink;
    private String dealID;
    private String storeID;
    private String gameID;
    private String salePrice;          // viene como String
    private String normalPrice;        // viene como String
    private String isOnSale;           // viene como "1" o "0" (String)
    private String savings;            // viene como String con decimales
    private String metacriticScore;
    private String steamRatingText;
    private String steamRatingPercent;
    private String steamRatingCount;
    private String steamAppID;
    private long releaseDate;          // epoch seconds
    private long lastChange;           // epoch seconds
    private String dealRating;         // viene como String decimal
    private String thumb;
}
