package es.uca.gamebox.service;

import es.uca.gamebox.component.client.SteamApiClient;
import es.uca.gamebox.dto.SteamOwnedGamesResponseDto;
import es.uca.gamebox.entity.*;
import es.uca.gamebox.repository.*;
import es.uca.gamebox.security.AuthenticatedUserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service("steam")
public class SteamLibrarySyncService implements GameLibrarySyncService{
    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameUserRepository gameUserRepository;

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private PlatformRepository platformRepository;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @Value("${steam.api.key}")
    private String apiKey;

    @Autowired
    private SteamApiClient steamApiClient;

    @Override
    public void syncLibrary(String steamId, User currentUser) {

        Store steamPlatform = getSteamStore();

        Library steamLibrary = obtainLibrary(currentUser, steamPlatform);

        List<SteamOwnedGamesResponseDto.Game> ownedGames = steamApiClient.getOwnedGames(steamId);

        linkOwnedGames(steamLibrary, ownedGames);

        this.syncFriends(currentUser);
    }

    private Store getSteamStore() {
        return storeRepository.findByNameIgnoreCase("Steam")
                .orElseThrow(() -> new RuntimeException("Steam store not found"));
    }

    private Library obtainLibrary(User user, Store steamStore) {
        List<Library> libraries = libraryRepository.findByUserIdAndStore(user.getId(), steamStore);
        if (!libraries.isEmpty()) {
            return libraries.getFirst();
        }
        else{
            Library lib = new Library();
            lib.setName("Biblioteca de Steam");
            lib.setUser(user);
            lib.setStore(steamStore);
            lib.setStore(storeRepository.findByNameIgnoreCase("Steam")
                    .orElseThrow(() -> new RuntimeException("Steam store not found")));
            lib.setCreatedAt(new java.util.Date());
            return libraryRepository.save(lib);
        }
    }

    private void linkOwnedGames(Library library, List<SteamOwnedGamesResponseDto.Game> steamGames) {
        // Mapea los AppIds para buscarlos en la base de datos
        List<String> appIds = steamGames.stream()
                .map(g -> String.valueOf(g.getAppid()))
                .toList();

        List<Game> dbGames = gameRepository.findBySteamAppIdIn(appIds);

        Map<String, Game> appIdToGameMap = new HashMap<>();
        for (Game game : dbGames) {
            appIdToGameMap.put(game.getSteamAppId(), game);
        }

        for (SteamOwnedGamesResponseDto.Game steamGame : steamGames) {
            Game game = appIdToGameMap.get(String.valueOf(steamGame.getAppid()));
            if (game == null) continue;

            boolean alreadyExists = gameUserRepository.existsByLibraryAndGame(library, game);
            if (!alreadyExists) {
                GameUser gu = new GameUser();
                gu.setLibrary(library);
                gu.setGame(game);
                gu.setSynced(true);
                gu.setCreatedAt(LocalDateTime.now());

                // Convertir minutos a horas (como float)
                gu.setHoursPlayed(steamGame.getPlaytime_forever() / 60.0f);

                // Convertir epoch a LocalDateTime
                if (steamGame.getRtime_last_played() > 0) {
                    gu.setLastPlayed(LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(steamGame.getRtime_last_played()),
                            ZoneId.systemDefault()
                    ));
                }

                gameUserRepository.save(gu);
            }
        }
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

    @Transactional
    public void unlinkSteamAccount(UUID userId) {
        Store steamStore = storeRepository.findByNameIgnoreCase("Steam")
                .orElseThrow(() -> new RuntimeException("Steam store not found"));

        //Obtener las bibliotecas del usuario que pertenezcan a Steam
        List<Library> steamLibraries = libraryRepository.findByUserIdAndStore(userId, steamStore);

        //Obtener los GameUser sincronizados automáticamente para esas bibliotecas
        List<GameUser> syncedGameUsers = gameUserRepository.findByLibraryInAndSyncedTrue(steamLibraries);

        gameUserRepository.deleteAll(syncedGameUsers);
    }

}
