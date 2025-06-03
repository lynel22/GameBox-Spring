package es.uca.gamebox.dto.rawg;

import lombok.Data;

import java.util.List;

@Data
public class RawgAllStoresResponseDto {
    private int count;
    private String next;
    private String previous;
    private List<RawgStoreDto> results;
}
