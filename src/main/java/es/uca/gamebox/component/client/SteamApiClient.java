package es.uca.gamebox.component.client;

import es.uca.gamebox.dto.steam.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SteamApiClient {

    @Value("${steam.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<String> getOwnedGameAppIds(String steamId) {
        String url = String.format(
                "https://api.steampowered.com/IPlayerService/GetOwnedGames/v1/?key=%s&steamid=%s&include_appinfo=true&include_played_free_games=true&include_gameplay_stats=true",
                apiKey, steamId
        );

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        Map<String, Object> responseMap = (Map<String, Object>) response.get("response");
        List<Map<String, Object>> games = (List<Map<String, Object>>) responseMap.get("games");

        return games.stream()
                .map(game -> ((Number) game.get("appid")).toString())
                .toList();
    }

    public List<SteamOwnedGamesResponseDto.Game> getOwnedGames(String steamId) {
        String url = String.format(
                "https://api.steampowered.com/IPlayerService/GetOwnedGames/v1/?key=%s&steamid=%s&include_appinfo=true&include_played_free_games=true&include_gameplay_stats=true",
                apiKey, steamId
        );

        ResponseEntity<SteamOwnedGamesResponseDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        SteamOwnedGamesResponseDto body = response.getBody();
        if (body != null && body.getResponse() != null && body.getResponse().getGames() != null) {
            return body.getResponse().getGames();
        }
        return List.of();
    }

    public List<String> getFriendsSteamIds(String steamId) {
        String url = String.format("http://api.steampowered.com/ISteamUser/GetFriendList/v0001/?key=%s&steamid=%s&relationship=friend", apiKey, steamId);
        System.out.println("url api frinds: " + url);
        ResponseEntity<SteamFriendsResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<SteamFriendsResponse>() {}
        );

        if (response.getBody() != null && response.getBody().getFriendslist() != null) {
            return response.getBody().getFriendslist().getFriends().stream()
                    .map(Friend::getSteamid)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    public List<SteamPlayerAchievementDto> getUnlockedAchievements(String steamId, Long appId) {
        System.out.printf("Syncing achievements: steamId=%s, appId=%d%n", steamId, appId);

        String url = String.format(
                "https://api.steampowered.com/ISteamUserStats/GetPlayerAchievements/v1/?key=%s&steamid=%s&appid=%d&l=english",
                apiKey, steamId, appId
        );

        SteamPlayerAchievementsResponseDto response = restTemplate.getForObject(url, SteamPlayerAchievementsResponseDto.class);

        if (response != null &&
                response.getPlayerstats() != null &&
                response.getPlayerstats().getAchievements() != null) {

            return response.getPlayerstats().getAchievements().stream()
                    .filter(a -> a.getAchieved() == 1)
                    .toList();
        }

        return List.of(); // Vacía si no hay logros desbloqueados
    }

}
