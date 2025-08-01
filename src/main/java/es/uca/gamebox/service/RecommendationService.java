package es.uca.gamebox.service;

import es.uca.gamebox.entity.*;
import es.uca.gamebox.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final GameUserRepository gameUserRepository;

    @Autowired
    private final GameRepository gameRepository;

    @Autowired
    private final GameReviewRepository gameReviewRepository;

    @Autowired
    private final RecommendationRepository recommendationRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * MON")
    public void generateWeeklyRecommendations() {
        LocalDate today = LocalDate.now();

        // Borrar recomendaciones previas para este día
        recommendationRepository.deleteByRecommendationDate(today);

        List<User> users = userRepository.findAll();

        for (User user : users) {
            List<GameUser> userGames = gameUserRepository.findByLibrary_User(user);
            Set<UUID> ownedGameIds = userGames.stream()
                    .map(gu -> gu.getGame().getId())
                    .collect(Collectors.toSet());

            // Calcular tiempo jugado por género
            Map<Genre, Float> genreHours = new HashMap<>();
            for (GameUser gameUser : userGames) {
                for (Genre genre : gameUser.getGame().getGenres()) {
                    genreHours.put(genre, genreHours.getOrDefault(genre, 0f) + gameUser.getHoursPlayed());
                }
            }

            List<Genre> topGenres = genreHours.entrySet().stream()
                    .sorted((e1, e2) -> Float.compare(e2.getValue(), e1.getValue()))
                    .limit(6) // Top 6 géneros mas jugados por el usuario
                    .map(Map.Entry::getKey)
                    .toList();

            if (topGenres.isEmpty()) continue;

            // Reviews positivas por juego
            Map<UUID, Long> positiveCounts = gameReviewRepository.findAll().stream()
                    .filter(GameReview::isRecommended)
                    .collect(Collectors.groupingBy(
                            r -> r.getGame().getId(),
                            Collectors.counting()
                    ));

            List<Recommendation> recommendations = new ArrayList<>();

            for (Genre genre : topGenres) {
                List<Game> gamesByGenre = gameRepository.findAllByGenres_Id(genre.getId()).stream()
                        .filter(g -> !ownedGameIds.contains(g.getId()))
                        .distinct()
                        .sorted((g1, g2) -> {
                            long c1 = positiveCounts.getOrDefault(g1.getId(), 0L);
                            long c2 = positiveCounts.getOrDefault(g2.getId(), 0L);
                            return Long.compare(c2, c1);
                        })
                        .limit(1) // Seleccionamos solo 1 juego por género
                        .toList();

                for (Game game : gamesByGenre) {
                    Recommendation r = new Recommendation();
                    r.setUser(user);
                    r.setGame(game);
                    r.setGenre(genre); // Asignamos el género en base al cual se recomendó
                    r.setRecommendationDate(today);
                    recommendations.add(r);
                }
            }

            recommendationRepository.saveAll(recommendations);
        }
    }
}
