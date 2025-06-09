package es.uca.gamebox.service;

import es.uca.gamebox.dto.GameDto;
import es.uca.gamebox.entity.Game;

import es.uca.gamebox.entity.User;
import es.uca.gamebox.mapper.GameMapper;
import es.uca.gamebox.repository.GameUserRepository;
import es.uca.gamebox.repository.LibraryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameService {

    @Autowired
    GameUserRepository gameUserRepository;

    public List<GameDto> getLibrary(User currentUser) {
        List<Game> games = gameUserRepository.findGamesByUser(currentUser);
        return GameMapper.toDtoList(games);
    }

    public List<GameDto> getGamesByStoreName(User user, String storeName) {
        List<Game> games = gameUserRepository.findGamesByUserAndStoreName(user, storeName);
        return GameMapper.toDtoList(games);
    }

}
