package es.uca.gamebox.dto;

import es.uca.gamebox.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
public class UserDto {
    private UUID id;
    private String username;
    private String email;
    private String imageUrl;
    private String steamId;

    private int level; // si lo tienes calculado en algún sitio
    private String title; // lo mismo

    public UserDto(User user) {
        this.id = user.getId();
        this.username = user.getRealUserName(); // usa el getter mapeado
        this.email = user.getEmail();
        this.imageUrl = user.getImageUrl();
        this.steamId = user.getSteamId();
        // Si tuvieras nivel y título calculados o almacenados, añade aquí
        /*this.level = 1; // placeholder
        this.title = "Gamer sin título"; // placeholder*/
    }

}
