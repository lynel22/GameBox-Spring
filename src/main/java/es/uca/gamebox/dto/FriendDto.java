package es.uca.gamebox.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class FriendDto {
    private UUID id;
    private String username;
    private String imageUrl;
}
