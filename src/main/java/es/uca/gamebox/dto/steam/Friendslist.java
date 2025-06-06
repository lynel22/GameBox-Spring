package es.uca.gamebox.dto.steam;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Friendslist {
    @JsonProperty("friends")
    private List<Friend> friends;
}
