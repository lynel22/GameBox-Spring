package es.uca.gamebox.component.client;


import es.uca.gamebox.dto.cheapshark.CheapSharkDealDto;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;

@Component
public class CheapSharkApiClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String BASE_URL = "https://www.cheapshark.com/api/1.0/deals";

    public List<CheapSharkDealDto> getAllDeals() {
        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("storeID", "1,7,25") // Steam, GOG, Epic
                .queryParam("pageSize", "60")
                .toUriString();

        CheapSharkDealDto[] deals = restTemplate.getForObject(url, CheapSharkDealDto[].class);
        return Arrays.asList(deals != null ? deals : new CheapSharkDealDto[0]);
    }

}