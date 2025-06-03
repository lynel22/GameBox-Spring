package es.uca.gamebox.service;

import es.uca.gamebox.entity.Game;
import es.uca.gamebox.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LibrarySyncManager {

    private final Map<String, GameLibrarySyncService> services;

    @Autowired
    public LibrarySyncManager(List<GameLibrarySyncService> serviceList) {
        this.services = serviceList.stream()
                .collect(Collectors.toMap(s -> s.getClass().getAnnotation(Service.class).value(), s -> s));
    }

    public void sync(String platform, String userPlatformId, User user) {
        GameLibrarySyncService service = services.get(platform);
        if (service == null) {
            throw new UnsupportedOperationException("Platform not supported: " + platform);
        }
        service.syncLibrary(userPlatformId, user);
    }
}
