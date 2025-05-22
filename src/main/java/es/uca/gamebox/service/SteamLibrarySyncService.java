package es.uca.gamebox.service;

import es.uca.gamebox.entity.Game;
import es.uca.gamebox.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("steam")
public class SteamLibrarySyncService implements GameLibrarySyncService{
    @Autowired
    private GameRepository gameRepository;

    @Override
    public List<Game> syncLibrary(String steamId) {
        // Lógica de llamada a la API de Steam y mapeo a GameDTO
        return null;
    }
}
