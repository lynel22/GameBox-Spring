package es.uca.gamebox.service;

import es.uca.gamebox.component.client.SteamApiClient;
import es.uca.gamebox.dto.steam.SteamOwnedGamesResponseDto;
import es.uca.gamebox.dto.steam.SteamPlayerAchievementDto;
import es.uca.gamebox.entity.*;
import es.uca.gamebox.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
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
    private AchievementUserRepository achievementUserRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    @Override
    public String getPlatform() {
        return "steam";
    }

    @Autowired
    private SteamApiClient steamApiClient;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    @Override
    public void syncLibrary(String steamId, User currentUser) {

        Store steamPlatform = getSteamStore();

        Library steamLibrary = obtainLibrary(currentUser, steamPlatform);

        List<SteamOwnedGamesResponseDto.Game> ownedGames = steamApiClient.getOwnedGames(steamId);

        linkOwnedGames(steamLibrary, ownedGames);

        this.syncFriends(currentUser, steamId);
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
        List<String> appIds = steamGames.stream()
                .map(g -> String.valueOf(g.getAppid()))
                .toList();

        List<Game> dbGames = gameRepository.findBySteamAppIdIn(appIds);

        Map<String, Game> appIdToGameMap = new HashMap<>();
        for (Game game : dbGames) {
            appIdToGameMap.put(game.getSteamAppId(), game);
        }

        int delayIndex = 0; // Contador de delay por juego

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
                gu.setHoursPlayed(steamGame.getPlaytime_forever() / 60.0f);

                if (steamGame.getRtime_last_played() > 0) {
                    gu.setLastPlayed(LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(steamGame.getRtime_last_played()),
                            ZoneId.systemDefault()
                    ));
                }

                gameUserRepository.save(gu);

                // Sincronizar logros en segundo plano con delay incremental
                final Game finalGame = game;
                final GameUser finalGu = gu;
                int delaySeconds = delayIndex++;
                String steamId = library.getUser().getSteamId();

                scheduler.schedule(() -> {
                    try {
                        syncUserAchievementsForGame(finalGame, finalGu, steamId);
                    } catch (Exception e) {
                        System.err.println("Error sincronizando logros para el juego " + finalGame.getName());
                        e.printStackTrace();
                    }
                }, delaySeconds, TimeUnit.SECONDS);
            }
        }
    }


    public void syncFriends(User currentUser, String steamId) {
        List<String> steamFriendIds = steamApiClient.getFriendsSteamIds(steamId);
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

        //Eliminar los AchievementUser asociados a esos GameUser
        achievementUserRepository.deleteByGameUserIn(syncedGameUsers);

        gameUserRepository.deleteAll(syncedGameUsers);
    }

    private void syncUserAchievementsForGame(Game game, GameUser gameUser, String steamId) {
        if (game.getSteamAppId() == null) return;

        List<SteamPlayerAchievementDto> unlockedAchievements;

        try {
            unlockedAchievements = steamApiClient.getUnlockedAchievements(steamId, Long.parseLong(game.getSteamAppId()));
        } catch (HttpClientErrorException e) {
            // Manejo específico de "Requested app has no stats"
            if (e.getResponseBodyAsString().contains("\"Requested app has no stats\"")) {
                System.out.printf("El juego '%s' (appId=%s) no tiene logros disponibles en la API de Steam.%n",
                        game.getName(), game.getSteamAppId());
                return;
            } else {
                throw e; // Propagamos otras excepciones que no son esperadas
            }
        }

        if (unlockedAchievements == null || unlockedAchievements.isEmpty()) return;

        for (SteamPlayerAchievementDto achievement : unlockedAchievements) {
            System.out.printf("Achievement unlocked: %s for game %s%n", achievement.getName(), game.getName());
        }

        List<Achievement> dbAchievements = achievementRepository.findByGameAndNameIn(
                game, unlockedAchievements.stream().map(SteamPlayerAchievementDto::getName).toList()
        );

        Map<String, Achievement> nameToAchievementMap = dbAchievements.stream()
                .collect(Collectors.toMap(Achievement::getName, a -> a));

        for (SteamPlayerAchievementDto steamAch : unlockedAchievements) {
            Achievement matchedAchievement = nameToAchievementMap.get(steamAch.getName());
            if (matchedAchievement == null) continue;

            boolean alreadyExists = achievementUserRepository.existsByUserAndAchievementAndGameUser(
                    gameUser.getLibrary().getUser(), matchedAchievement, gameUser);

            if (!alreadyExists) {
                AchievementUser au = new AchievementUser();
                au.setAchievement(matchedAchievement);
                au.setUser(gameUser.getLibrary().getUser());
                au.setGameUser(gameUser);
                au.setDateUnlocked(steamAch.getUnlocktime() != null
                        ? LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(steamAch.getUnlocktime()), ZoneId.systemDefault())
                        : LocalDateTime.now()
                );

                achievementUserRepository.save(au);
            }
        }
    }




}
