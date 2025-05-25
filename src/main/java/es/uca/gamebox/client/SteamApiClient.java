package es.uca.gamebox.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class SteamApiClient {

    @Value("${steam.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<String> getOwnedGameAppIds(String steamId) {
        String url = String.format(
                "https://api.steampowered.com/IPlayerService/GetOwnedGames/v1/?key=%s&steamid=%s&include_appinfo=true",
                apiKey, steamId
        );

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        Map<String, Object> responseMap = (Map<String, Object>) response.get("response");
        List<Map<String, Object>> games = (List<Map<String, Object>>) responseMap.get("games");

        return games.stream()
                .map(game -> ((Number) game.get("appid")).toString())
                .toList();
    }
}
