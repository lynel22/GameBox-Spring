package es.uca.gamebox.dto.steam;

import lombok.Data;

@Data
public class Friend {
    private String steamid;
    private String relationship;
    private long friend_since;
}
