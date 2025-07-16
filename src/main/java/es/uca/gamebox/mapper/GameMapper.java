package es.uca.gamebox.mapper;

import es.uca.gamebox.dto.*;
import es.uca.gamebox.entity.*;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
public class GameMapper {

    public static GameDto toDto(Game game) {
        GameDto dto = new GameDto();
        dto.setId(game.getId());
        dto.setName(game.getName());
        dto.setDescription(game.getDescription());
        dto.setImageUrl(game.getImageUrl());
        dto.setReleaseDate(game.getReleaseDate().toString());

        if (game.getDeveloper() != null) {
            DeveloperDto developerDto = new DeveloperDto();
            developerDto.setId(game.getDeveloper().getId());
            developerDto.setName(game.getDeveloper().getName());
            dto.setDeveloper(developerDto);
        } else {
            dto.setDeveloper(null); 
        }

        List<GenreDto> genreDtos = game.getGenres().stream().map(genre -> {
            GenreDto g = new GenreDto();
            g.setId(genre.getId());
            g.setName(genre.getName());
            g.setSpanishName(genre.getSpanishName());
            return g;
        }).collect(Collectors.toList());

        dto.setGenres(genreDtos);

        return dto;
    }


    public static List<GameDto> toDtoList(List<Game> games) {
        return games.stream().map(GameMapper::toDto).collect(Collectors.toList());
    }


    public static GameDetailDto toGameDetailDto(
            Game game,
            List<Achievement> allAchievements,
            List<AchievementUser> unlockedAchievements,
            List<User> friendsWithGame,
            GameUser gameUser,
            boolean inWishlist
    ) {
        GameDetailDto dto = new GameDetailDto();

        dto.setId(game.getId());
        dto.setName(game.getName());
        dto.setDescription(game.getDescription());
        dto.setImageUrl(game.getImageUrl());
        dto.setReleaseDate(game.getReleaseDate());

        // Developer
        if (game.getDeveloper() != null) {
            DeveloperDto developerDto = new DeveloperDto();
            developerDto.setId(game.getDeveloper().getId());
            developerDto.setName(game.getDeveloper().getName());
            dto.setDeveloper(developerDto);
        }

        // Genres
        dto.setGenres(game.getGenres().stream().map(genre -> {
            GenreDto genreDto = new GenreDto();
            genreDto.setId(genre.getId());
            genreDto.setName(genre.getName());
            genreDto.setSpanishName(genre.getSpanishName());
            return genreDto;
        }).collect(Collectors.toList()));

        // Achievements
        dto.setAchievements(allAchievements.stream().map(achievement -> {
            AchievementDto a = new AchievementDto();
            a.setId(achievement.getId());
            a.setName(achievement.getName());
            a.setDescription(achievement.getDescription());
            a.setImageUrl(achievement.getImageUrl());

            // Buscar si está desbloqueado
            AchievementUser matched = unlockedAchievements.stream()
                    .filter(au -> au.getAchievement().getId().equals(achievement.getId()))
                    .findFirst()
                    .orElse(null);

            if (matched != null) {
                a.setUnlocked(true);
                a.setDateUnlocked(matched.getDateUnlocked());
            } else {
                a.setUnlocked(false);
                a.setDateUnlocked(null);
            }

            return a;
        }).collect(Collectors.toList()));

        // Friends
        dto.setFriendsThatOwnIt(friendsWithGame.stream().map(friend -> {
            FriendDto f = new FriendDto();
            f.setId(friend.getId());
            f.setUsername(friend.getRealUserName());
            f.setImageUrl(friend.getImageUrl());
            return f;
        }).collect(Collectors.toList()));

        // Stores
        dto.setStores(game.getStores().stream().map(store -> {
            StoreDto storeDto = new StoreDto();
            storeDto.setId(store.getId());
            storeDto.setName(store.getName());
            storeDto.setImageUrl(store.getImageUrl());
            return storeDto;
        }).collect(Collectors.toList()));


        // Last played and hours played
        if (gameUser != null) {
            dto.setLastPlayed(gameUser.getLastPlayed());
            dto.setHoursPlayed(gameUser.getHoursPlayed());
            dto.setOwnedByUser(true);
        }
        else {
            dto.setOwnedByUser(false);
        }

        dto.setInWishlist(inWishlist);

        // Deals
        dto.setDeals(game.getDeals().stream().map(deal -> new DealDto(
                game.getName(),
                game.getId().toString(),
                game.getImageUrl(),
                deal.getNormalPrice(),
                deal.getSalePrice(),
                deal.getSavings(),
                deal.getStore() != null ? deal.getStore().getName() : null,
                deal.getDealUrl()
        )).toList());

        return dto;
    }

    public static GameWishlistDto toGameWishlistDto(Wishlist wishlist) {
        Game game = wishlist.getGame();

        GameWishlistDto dto = new GameWishlistDto();
        dto.setId(game.getId());
        dto.setName(game.getName());
        dto.setImageUrl(game.getImageUrl());
        dto.setReleaseDate(game.getReleaseDate());
        dto.setFechaAdicion(wishlist.getFechaAdicion());

        dto.setGenres(game.getGenres().stream().map(genre -> {
            GenreDto g = new GenreDto();
            g.setId(genre.getId());
            g.setName(genre.getName());
            g.setSpanishName(genre.getSpanishName());
            return g;
        }).collect(Collectors.toList()));

        dto.setStores(game.getStores().stream().map(store -> {
            StoreDto s = new StoreDto();
            s.setId(store.getId());
            s.setName(store.getName());
            s.setImageUrl(store.getImageUrl());
            return s;
        }).collect(Collectors.toList()));

        dto.setDeals(game.getDeals().stream().map(deal ->
                new DealDto(
                        game.getName(),
                        game.getId().toString(),
                        game.getImageUrl(),
                        deal.getNormalPrice(),
                        deal.getSalePrice(),
                        deal.getSavings(),
                        deal.getStore() != null ? deal.getStore().getName() : null,
                        deal.getDealUrl()
                )
        ).collect(Collectors.toList()));


        return dto;
    }


}
