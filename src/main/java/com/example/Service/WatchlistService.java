package com.example.Service;

import com.example.Exception.AlphaVantageException;
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
    public Watchlist addStockToWatchlist(Long watchlistId, Long userId, String symbol) {
        Watchlist watchlist = watchlistRepository.findByIdAndUserId(watchlistId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Watchlist non trovata o non appartiene all'utente"));

        Stock existingStock = watchlistRepository.getStocksFromWatchList(watchlistId, userId, symbol);
        if (existingStock != null) {
            throw new IllegalArgumentException("Il simbolo " + symbol + " è già presente nella watchlist");
        }

        try {
            Stock stockDetails = alphaVantageService.getStockDetails(symbol);
            watchlist.addStock(stockDetails);
            return watchlistRepository.save(watchlist);
        }
        catch (AlphaVantageException e) {
            // Gestisci specificamente le eccezioni di AlphaVantage e fornisci messaggi di errore più utili
            switch (e.getErrorType()) {
                case API_LIMIT_EXCEEDED:
                    throw new IllegalArgumentException("Limite di chiamate API superato. Riprova più tardi.");
                case INVALID_API_KEY:
                    throw new IllegalArgumentException("Problema di autenticazione con il servizio di quotazioni.");
                case NO_DATA_FOUND:
                    throw new IllegalArgumentException("Nessun dato trovato per il simbolo: " + symbol);
                case SERVICE_UNAVAILABLE:
                    throw new IllegalArgumentException("Servizio di quotazioni non disponibile. Riprova più tardi.");
                case NETWORK_ERROR:
                    throw new IllegalArgumentException("Errore di connessione al servizio di quotazioni.");
                case INVALID_RESPONSE:
                    throw new IllegalArgumentException("Risposta non valida dal servizio di quotazioni per: " + symbol);
                default:
                    throw new IllegalArgumentException("Errore durante il recupero dei dettagli del simbolo: " + symbol);
            }
        }
        catch (Exception e) {
            // Log dell'eccezione per debug
            System.err.println("Errore imprevisto durante l'aggiunta del simbolo " + symbol + ": " + e.getMessage());
            e.printStackTrace();
            throw new IllegalArgumentException("Errore imprevisto durante l'aggiunta del simbolo: " + symbol);
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
