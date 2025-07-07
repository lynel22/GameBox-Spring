package es.uca.gamebox.service;


import es.uca.gamebox.component.client.CheapSharkApiClient;
import es.uca.gamebox.dto.cheapshark.CheapSharkDealDto;
import es.uca.gamebox.entity.Deal;
import es.uca.gamebox.entity.Game;
import es.uca.gamebox.entity.Store;
import es.uca.gamebox.repository.DealRepository;
import es.uca.gamebox.repository.GameRepository;
import es.uca.gamebox.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DealService {

    private final CheapSharkApiClient cheapSharkApiClient;
    private final DealRepository dealRepository;
    private final GameRepository gameRepository;
    private final StoreRepository storeRepository;

    @Scheduled(cron = "0 0 13 * * *")
    public void syncDailyDeals() {
        List<CheapSharkDealDto> fetchedDeals = cheapSharkApiClient.getAllDeals();
        Instant now = Instant.now();

        for (CheapSharkDealDto dto : fetchedDeals) {
            UUID dealUUID = UUID.nameUUIDFromBytes(dto.getDealID().getBytes());

            Deal deal = dealRepository.findById(dealUUID).orElseGet(() -> {
                Deal newDeal = new Deal();
                newDeal.setDealID(dealUUID);
                newDeal.setFirstSeen(now);
                return newDeal;
            });

            deal.setTitle(dto.getTitle());
            deal.setNormalPrice(new BigDecimal(dto.getNormalPrice()));
            deal.setSalePrice(new BigDecimal(dto.getSalePrice()));
            deal.setSavings(new BigDecimal(dto.getSavings()));
            deal.setLastSeen(now);

            // Asignar Game por título si existe (ignorar mayúsculas/minúsculas)
            Optional<Game> gameOpt = gameRepository.findByNameIgnoreCase(dto.getTitle());
            gameOpt.ifPresent(deal::setGame);

            // Asignar Store si existe
            Optional<Store> storeOpt = storeRepository.findByCheapSharkStoreId(dto.getStoreID());
            storeOpt.ifPresent(deal::setStore);

            dealRepository.save(deal);
        }

        // Marcar como caducadas las que no han sido vistas hoy
        Instant threshold = now.minusSeconds(60 * 60 * 24);
        List<Deal> expired = dealRepository.findAllByLastSeenBeforeAndEndDateIsNull(threshold);
        for (Deal d : expired) {
            d.setEndDate(d.getLastSeen());
        }
        dealRepository.saveAll(expired);
    }
}
