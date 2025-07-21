package es.uca.gamebox.dto;

import es.uca.gamebox.entity.User;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UserProfileDto {
    private UUID id;
    private String username;
    private String email;
    private String imageUrl;
    private String steamId;
    private List<FriendDto> friends;
    private int totalGames;
    private List<GameSummaryDto> games;
    private String avatarUrl;

    public UserProfileDto(User user, List<GameSummaryDto> games) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.avatarUrl = user.getImageUrl();
        this.games = games;
    }

    public UserProfileDto() {}
}
