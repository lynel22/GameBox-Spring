package es.uca.gamebox.dto.rawg;

import lombok.Data;

import java.util.List;

@Data
public class RawgStoresResponseDto {
    private List<RawgGameStoreEntryDto> results;
}
