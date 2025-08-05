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

    @Scheduled(cron = "0 0 * * * *")
    public void syncDailyDeals() {
        System.out.println(">>> Ejecutando syncDailyDeals");
        List<CheapSharkDealDto> fetchedDeals = cheapSharkApiClient.getAllDeals();
        Instant now = Instant.now();

        for (CheapSharkDealDto dto : fetchedDeals) {
            Optional<Game> gameOpt = gameRepository.findByNameIgnoreCase(dto.getTitle());
            if (gameOpt.isEmpty()) {
                System.out.println("No se encontró juego para oferta: " + dto.getTitle());
                continue;
            }

            Deal deal = dealRepository.findByCheapSharkID(dto.getDealID()).orElseGet(() -> {
                Deal newDeal = new Deal();
                newDeal.setCheapSharkID(dto.getDealID());
                newDeal.setFirstSeen(now);
                return newDeal;
            });

            System.out.println("Procesando oferta: " + dto.getTitle() + " - " + dto.getDealID());


            deal.setNormalPrice(new BigDecimal(dto.getNormalPrice()));
            deal.setSalePrice(new BigDecimal(dto.getSalePrice()));
            deal.setSavings(new BigDecimal(dto.getSavings()));
            deal.setLastSeen(now);
            try {
                Thread.sleep(300);
                String dealUrl = cheapSharkApiClient.getDealLink(dto.getDealID());
                deal.setDealUrl(dealUrl);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread interrumpido al esperar para evitar 429.");
            }

            deal.setGame(gameOpt.get());

            Optional<Store> storeOpt = storeRepository.findByCheapSharkStoreId(dto.getStoreID());
            storeOpt.ifPresent(deal::setStore);

            dealRepository.save(deal);

            System.out.println("Oferta guardada: " + deal.getGame().getName() + " - " + deal.getSalePrice() + " en " + (deal.getStore() != null ? deal.getStore().getName() : "Desconocido"));
        }

        Instant threshold = now.minusSeconds(60L * 60 * 24);
        List<Deal> expired = dealRepository.findAllByLastSeenBeforeAndEndDateIsNull(threshold);
        for (Deal d : expired) {
            d.setEndDate(d.getLastSeen());
        }
        dealRepository.saveAll(expired);
    }

}
