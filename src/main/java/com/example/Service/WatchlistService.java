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

    public Optional<Watchlist> getWatchListByIdUserAndUserId(Long watchlistId, Long userId){
        return watchlistRepository.findByIdAndUserId(watchlistId,userId);
    }

    @Transactional
    public Watchlist createWatchList(Long userId, String name, String description){
        if (watchlistRepository.existByNameAndUserId(name, userId)) {
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

        Stock stockDetails = alphaVantageService.getStockDetails(symbol);

        watchlist.addStock(stockDetails);
        return watchlistRepository.save(watchlist);
    }

    @Transactional
    public Watchlist removeStockFromWatchlist(Long watchlistId, Long userId, String symbol){
        Watchlist watchlist = watchlistRepository.findByIdAndUserId(watchlistId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Watchlist non trovata o non appartiene all'utente"));

        final Stock[] result = new Stock[1];


        watchlistRepository.getStocksFromWatchList(watchlistId, userId).forEach(
                stock -> {
                    if (stock.getSymbol().equals(symbol)){
                       result[0] = stock;
                    }
                }
        );

        watchlist.removeStock(result[0]);
        return watchlistRepository.save(watchlist);
    }

}
