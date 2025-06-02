package es.uca.gamebox.dto.rawg;

import lombok.Data;
import java.util.List;

@Data
public class RawgAchievementResponse {
    private List<RawgAchievementDto> results;
}
