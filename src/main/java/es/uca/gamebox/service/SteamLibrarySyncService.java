package es.uca.gamebox.service;

import es.uca.gamebox.component.client.SteamApiClient;
import es.uca.gamebox.entity.Game;
import es.uca.gamebox.entity.User;
import es.uca.gamebox.repository.GameRepository;
import es.uca.gamebox.repository.UserRepository;
import es.uca.gamebox.security.AuthenticatedUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service("steam")
public class SteamLibrarySyncService implements GameLibrarySyncService{
    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @Value("${steam.api.key}")
    private String apiKey;

    @Autowired
    private SteamApiClient steamApiClient;

    @Override
    public void syncLibrary(String steamId) {
        User currentUser = authenticatedUserService.getAuthenticatedUser();


        List<String> appIds = steamApiClient.getOwnedGameAppIds(steamId);
        List<Game> ownedGames = gameRepository.findBySteamAppIdIn(appIds);

        this.syncFriends(currentUser);
    }

    public void syncFriends(User currentUser) {
        List<String> steamFriendIds = steamApiClient.getFriendsSteamIds(currentUser.getSteamId());
        if (steamFriendIds.isEmpty()) return;

        List<User> registeredFriends = Collections.emptyList();

        Optional<List<User>> OptionalregisteredFriends = userRepository.findBySteamIdIn(steamFriendIds);
        if (OptionalregisteredFriends.isEmpty()) {
            return; // No friends found in the database
        }
        else{
            registeredFriends = OptionalregisteredFriends.get();
        }

        for (User friend : registeredFriends) {
            if (!currentUser.getFriends().contains(friend)) {
                currentUser.getFriends().add(friend);
                friend.getFriends().add(currentUser); // relación bidireccional
            }
        }

        userRepository.save(currentUser);
        userRepository.saveAll(registeredFriends);
    }
}
