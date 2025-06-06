package es.uca.gamebox.service;

import es.uca.gamebox.entity.Game;
import es.uca.gamebox.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface GameLibrarySyncService {
    String getPlatform();
    void syncLibrary(String userId, User user);
}
