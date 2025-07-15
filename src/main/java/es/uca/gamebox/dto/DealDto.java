package es.uca.gamebox.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class DealDto {
    private String gameTitle;
    private String gameId;
    private String gameImageUrl;
    private BigDecimal normalPrice;
    private BigDecimal salePrice;
    private BigDecimal savings;
    private String storeName;
    private String dealUrl;
}
