package es.uca.gamebox.startup;

import es.uca.gamebox.service.GameSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameStartupRunner {

    private final GameSyncService gameSyncService;

    /*@EventListener(ApplicationReadyEvent.class)*/
    public void onApplicationReady() {
        log.info("Ejecutando sincronización de juegos sin tiendas...");
        gameSyncService.updateGamesWithoutStores();
    }
}
