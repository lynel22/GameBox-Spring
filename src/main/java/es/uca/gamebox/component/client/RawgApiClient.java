package es.uca.gamebox.component.client;



import es.uca.gamebox.dto.rawg.RawgAchievementResponse;
import es.uca.gamebox.dto.rawg.RawgGameDetailDto;
import es.uca.gamebox.dto.rawg.RawgGamesResponse;
import es.uca.gamebox.dto.rawg.RawgStoresResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class RawgApiClient {

    private final RestTemplate restTemplate;

    @Value("${rawg.api.key}")
    private String apiKey;

    private static final String BASE_URL = "https://api.rawg.io/api";
    private static final String DEFAULT_LANG = "es";

    public RawgApiClient() {
        this.restTemplate = new RestTemplate();
    }

    public RawgGamesResponse getGames(int page, int pageSize) {
        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/games")
                .queryParam("key", apiKey)
                .queryParam("page", page)
                .queryParam("page_size", pageSize)
                .queryParam("ordering", "-added")
                .queryParam("lang", DEFAULT_LANG)
                .toUriString();

        return restTemplate.getForObject(url, RawgGamesResponse.class);
    }

    public RawgGameDetailDto getGameDetails(String slug) {
        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/games/{slug}")
                .queryParam("key", apiKey)
                .queryParam("lang", DEFAULT_LANG)
                .buildAndExpand(slug)
                .toUriString();

        return restTemplate.getForObject(url, RawgGameDetailDto.class);
    }

    public RawgAchievementResponse getAchievementsForGame(String slug) {
        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/games/{slug}/achievements")
                .queryParam("key", apiKey)
                .queryParam("lang", DEFAULT_LANG)
                .buildAndExpand(slug)
                .toUriString();

        return restTemplate.getForObject(url, RawgAchievementResponse.class);
    }

    public RawgStoresResponseDto getGameStores(String slug) {
        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/games/{slug}/stores")
                .queryParam("key", apiKey)
                .queryParam("lang", DEFAULT_LANG)
                .buildAndExpand(slug)
                .toUriString();

        return restTemplate.getForObject(url, RawgStoresResponseDto.class);
    }

}
