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

    @Scheduled(cron = "0 57 13 * * *")
    public void syncDailyDeals() {
        List<CheapSharkDealDto> fetchedDeals = cheapSharkApiClient.getAllDeals();
        Instant now = Instant.now();

        for (CheapSharkDealDto dto : fetchedDeals) {
            UUID dealUUID = UUID.nameUUIDFromBytes(dto.getDealID().getBytes());

            // Asignar Game por título si existe
            Optional<Game> gameOpt = gameRepository.findByNameIgnoreCase(dto.getTitle());
            if (gameOpt.isEmpty()) {
                System.out.println("No se encontró juego para oferta: " + dto.getTitle());
                continue; // saltamos si no hay juego
            }

            Deal deal = dealRepository.findById(dealUUID).orElseGet(() -> {
                Deal newDeal = new Deal();
                newDeal.setDealID(dealUUID);
                newDeal.setFirstSeen(now);
                return newDeal;
            });

            deal.setNormalPrice(new BigDecimal(dto.getNormalPrice()));
            deal.setSalePrice(new BigDecimal(dto.getSalePrice()));
            deal.setSavings(new BigDecimal(dto.getSavings()));
            deal.setLastSeen(now);
            deal.setGame(gameOpt.get());

            // Asignar Store si existe
            Optional<Store> storeOpt = storeRepository.findByCheapSharkStoreId(dto.getStoreID());
            storeOpt.ifPresent(deal::setStore);

            dealRepository.save(deal);

            System.out.println("Oferta guardada: " + deal.getGame().getName() + " - " + deal.getSalePrice() + " en " + (deal.getStore() != null ? deal.getStore().getName() : "Desconocido"));
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
