package es.uca.gamebox.mapper;

import es.uca.gamebox.dto.DeveloperDto;
import es.uca.gamebox.dto.GameDto;
import es.uca.gamebox.dto.GenreSimpleDto;
import es.uca.gamebox.entity.Game;
import es.uca.gamebox.entity.Genre;
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

        List<GenreSimpleDto> genreDtos = game.getGenres().stream().map(genre -> {
            GenreSimpleDto g = new GenreSimpleDto();
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
}
