package es.uca.gamebox.service;

import es.uca.gamebox.entity.Game;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface GameLibrarySyncService {

    void syncLibrary(String userId);
}
