package es.uca.gamebox.dto.steam;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Friend {
    @JsonProperty("steamid")
    private String steamid;

    @JsonProperty("relationship")
    private String relationship;

    @JsonProperty("friend_since")
    private long friendSince;
}
