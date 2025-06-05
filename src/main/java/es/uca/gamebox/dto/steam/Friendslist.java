package es.uca.gamebox.dto.steam;

import lombok.Data;

import java.util.List;

@Data
public class Friendslist {
    private List<Friend> friends;
}
