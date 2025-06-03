package es.uca.gamebox.service;

import es.uca.gamebox.component.client.RawgApiClient;
import es.uca.gamebox.dto.rawg.RawgStoreDto;
import es.uca.gamebox.entity.Store;
import es.uca.gamebox.repository.StoreRepository;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreSyncService {

    private final RawgApiClient rawgApiClient;
    private final StoreRepository storeRepository;

    /*@PostConstruct*/
    public void syncStoresOnStartup() {
        List<RawgStoreDto> rawgStores = rawgApiClient.getStores(1, 40);

        for (RawgStoreDto rawgStore : rawgStores) {
            storeRepository.findByRawgId(rawgStore.getId()).ifPresentOrElse(
                    existing -> {
                        existing.setName(rawgStore.getName());
                        existing.setImageUrl(rawgStore.getImage_background());
                        existing.setRawgId(rawgStore.getId());
                        existing.setSlugRawg(rawgStore.getSlug());
                        existing.setDomain(rawgStore.getDomain());
                        storeRepository.save(existing);
                    },
                    () -> {
                        Store newStore = new Store();
                        newStore.setRawgId(rawgStore.getId());
                        newStore.setName(rawgStore.getName());
                        newStore.setSlugRawg(rawgStore.getSlug());
                        newStore.setDomain(rawgStore.getDomain());
                        newStore.setImageUrl(rawgStore.getImage_background());
                        storeRepository.save(newStore);
                    }
            );
        }
    }
}
