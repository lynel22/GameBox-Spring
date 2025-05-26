package es.uca.gamebox.service;

import es.uca.gamebox.client.SteamApiClient;
import es.uca.gamebox.dto.SteamOwnedGamesResponseDTO;
import es.uca.gamebox.entity.Game;
import es.uca.gamebox.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service("steam")
public class SteamLibrarySyncService implements GameLibrarySyncService{
    @Autowired
    private GameRepository gameRepository;

    @Value("${steam.api.key}")
    private String apiKey;

    @Autowired
    private SteamApiClient steamApiClient;

    @Override
    public List<Game> syncLibrary(String steamId) {
        List<String> appIds = steamApiClient.getOwnedGameAppIds(steamId);
        return gameRepository.findBySteamAppIdIn(appIds);
    }
}
