package com.example.Service;

import com.example.Model.Stock;
import com.example.Model.User;
import com.example.Model.Watchlist;
import com.example.Repository.UserRepository;
import com.example.Repository.WatchlistRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;
    private final AlphaVantageService alphaVantageService;

    public List<Watchlist> getWatchlistByUserId(Long userId){
        return watchlistRepository.findByUserId(userId);
    }

    public Optional<Watchlist> getWatchListByIdAndUserId(Long watchlistId, Long userId){
        return watchlistRepository.findByIdAndUserId(watchlistId,userId);
    }

    @Transactional
    public Watchlist createWatchList(Long userId, String name, String description){
        if (watchlistRepository.existsByNameAndUserId(name, userId)) {
            throw new IllegalArgumentException("Esiste già una watchlist con questo nome");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));

        Watchlist watchlist = Watchlist.builder()
                .name(name)
                .description(description)
                .user(user)
                .build();

        return watchlistRepository.save(watchlist);
    }

    @Transactional
    public void deleteWatchlist(Long watchlistId, Long userId) {
        Watchlist watchlist = watchlistRepository.findByIdAndUserId(watchlistId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Watchlist non trovata o non appartiene all'utente"));

        watchlistRepository.delete(watchlist);
    }

    @Transactional
    public Watchlist addStockToWatchlist(Long watchlistId, Long userId, String symbol){
        Watchlist watchlist = watchlistRepository.findByIdAndUserId(watchlistId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Watchlist non trovata o non appartiene all'utente"));

        Stock existingStock = watchlistRepository.getStocksFromWatchList(watchlistId, userId, symbol);
        if (existingStock != null) {
            throw new IllegalArgumentException("Il simbolo " + symbol + " è già presente nella watchlist");
        }

        try{
            Stock stockDetails = alphaVantageService.getStockDetails(symbol);
            watchlist.addStock(stockDetails);
            return watchlistRepository.save(watchlist);

        }
        catch (Exception e){
            throw new IllegalArgumentException("Simbolo non valido: " + symbol);
        }
    }

    @Transactional
    public Watchlist removeStockFromWatchlist(Long watchlistId, Long userId, String symbol){
        Watchlist watchlist = watchlistRepository.findByIdAndUserId(watchlistId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Watchlist non trovata o non appartiene all'utente"));

        Stock stockToRemove = watchlistRepository.getStocksFromWatchList(watchlistId, userId, symbol);

        if(!(stockToRemove ==null)){
            watchlist.removeStock(stockToRemove);
            return watchlistRepository.save(watchlist);
        }
        else {
            throw new IllegalArgumentException("Stco non presente in watchlist");
        }
    }

}
