package es.uca.gamebox.service;

import es.uca.gamebox.entity.Game;
import es.uca.gamebox.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface GameLibrarySyncService {

    List<Game> syncLibrary(String userId);
}
