package com.example.Service;

import com.example.Exception.AlphaVantageException;
import com.example.Model.Stock;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlphaVantageService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${alphavantage.api-key}")
    private String apiKey;

    @Value("${alphavantage.base-url}")
    private String baseUrl;

    public Stock getStockDetails(String symbol) {
        try {
            // Modifica l'URL per includere /query se non è già presente nel baseUrl
            String quoteUrl = baseUrl;
            if (!baseUrl.endsWith("/query")) {
                quoteUrl = baseUrl + "/query";
            }
            quoteUrl += "?function=GLOBAL_QUOTE&symbol=" + symbol + "&apikey=" + apiKey;

            log.info("Chiamata API: {}", quoteUrl.replace(apiKey, "API_KEY_HIDDEN"));
            ResponseEntity<String> quoteResponse = restTemplate.getForEntity(quoteUrl, String.class);

            // Verifica se la risposta è HTML invece di JSON
            String responseBody = quoteResponse.getBody();
            if (responseBody == null) {
                throw new AlphaVantageException(
                        "Risposta vuota dall'API Alpha Vantage",
                        AlphaVantageException.ErrorType.INVALID_RESPONSE
                );
            }

            if (responseBody.trim().startsWith("<")) {
                log.error("Ricevuta risposta HTML invece di JSON: {}", responseBody.substring(0, Math.min(responseBody.length(), 100)));
                throw new AlphaVantageException(
                        "Risposta non valida dall'API Alpha Vantage. Verifica la tua chiave API.",
                        AlphaVantageException.ErrorType.INVALID_RESPONSE
                );
            }

            // Controllo per limiti di API superati
            if (responseBody.contains("calls per day") || responseBody.contains("frequency")) {
                throw new AlphaVantageException(
                        "Limite di chiamate API Alpha Vantage superato. Riprova più tardi.",
                        AlphaVantageException.ErrorType.API_LIMIT_EXCEEDED
                );
            }

            log.debug("Risposta GLOBAL_QUOTE: {}", responseBody);
            JsonNode rootNode = objectMapper.readTree(responseBody);

            // Controlla se c'è un messaggio di errore
            if (rootNode.has("Information") || rootNode.has("Note")) {
                String errorInfo = rootNode.has("Information")
                        ? rootNode.get("Information").asText()
                        : rootNode.get("Note").asText();

                if (errorInfo.contains("API key")) {
                    throw new AlphaVantageException(
                            "Chiave API Alpha Vantage non valida o mancante",
                            AlphaVantageException.ErrorType.INVALID_API_KEY
                    );
                } else if (errorInfo.contains("calls per")) {
                    throw new AlphaVantageException(
                            "Limite di chiamate API Alpha Vantage superato: " + errorInfo,
                            AlphaVantageException.ErrorType.API_LIMIT_EXCEEDED
                    );
                }
            }

            JsonNode quoteNode = rootNode.path("Global Quote");

            if (quoteNode.isEmpty()) {
                log.warn("Nessun dato 'Global Quote' trovato per il simbolo: {}", symbol);
                throw new AlphaVantageException(
                        "Nessun dato trovato per il simbolo: " + symbol,
                        AlphaVantageException.ErrorType.NO_DATA_FOUND
                );
            }

            // Seconda chiamata per i dettagli dell'azienda
            String overviewUrl = baseUrl;
            if (!baseUrl.endsWith("/query")) {
                overviewUrl = baseUrl + "/query";
            }
            overviewUrl += "?function=OVERVIEW&symbol=" + symbol + "&apikey=" + apiKey;

            log.info("Chiamata API per dettagli azienda: {}", overviewUrl.replace(apiKey, "API_KEY_HIDDEN"));
            ResponseEntity<String> overviewResponse = restTemplate.getForEntity(overviewUrl, String.class);

            // Verifica anche questa risposta
            String overviewBody = overviewResponse.getBody();
            if (overviewBody == null) {
                log.warn("Risposta vuota dall'API Alpha Vantage per i dettagli dell'azienda");
                // Continua con dati parziali
            }

            if (overviewBody != null && overviewBody.trim().startsWith("<")) {
                log.warn("Ricevuta risposta HTML per i dettagli dell'azienda. Procedendo con dati parziali.");
                // Continua comunque con i dati della quotazione che abbiamo
            } else if (overviewBody != null) {
                log.debug("Risposta OVERVIEW: {}", overviewBody);
            }

            // Crea l'oggetto Stock con i dati disponibili
            Stock stock = new Stock();
            stock.setSymbol(symbol);

            // Prova a leggere i dati dell'overview, ma non fallire se non sono disponibili
            JsonNode overviewNode;
            try {
                if (overviewBody != null) {
                    overviewNode = objectMapper.readTree(overviewBody);
                    stock.setName(overviewNode.path("Name").asText(symbol));
                    stock.setExchangeName(overviewNode.path("Exchange").asText(""));
                    stock.setCurrency(overviewNode.path("Currency").asText("USD"));

                    String marketCapStr = overviewNode.path("MarketCapitalization").asText("0");
                    stock.setMarketCap(new BigDecimal(marketCapStr));
                } else {
                    // Imposta valori predefiniti se non ci sono dati dell'overview
                    stock.setName(symbol);
                    stock.setExchangeName("");
                    stock.setCurrency("USD");
                    stock.setMarketCap(BigDecimal.ZERO);
                }
            } catch (Exception e) {
                log.warn("Impossibile elaborare i dettagli dell'azienda, utilizzo valori predefiniti", e);
                stock.setName(symbol);
                stock.setExchangeName("");
                stock.setCurrency("USD");
                stock.setMarketCap(BigDecimal.ZERO);
            }

            // Estrai i dati dalla quotazione
            String priceStr = quoteNode.path("05. price").asText("0");
            stock.setCurrentPrice(new BigDecimal(priceStr));

            String changeStr = quoteNode.path("09. change").asText("0");
            String changePercentStr = quoteNode.path("10. change percent").asText("0%");
            // Rimuovi il simbolo % se presente
            changePercentStr = changePercentStr.replace("%", "");

            stock.setDailyChange(new BigDecimal(changeStr));
            stock.setDailyChangePercent(new BigDecimal(changePercentStr));

            String volumeStr = quoteNode.path("06. volume").asText("0");
            stock.setVolume(new BigDecimal(volumeStr));

            return stock;

        } catch (ResourceAccessException e) {
            log.error("Errore di connessione al servizio Alpha Vantage: {}", e.getMessage(), e);
            throw new AlphaVantageException(
                    "Impossibile connettersi al servizio Alpha Vantage. Verifica la tua connessione e riprova.",
                    AlphaVantageException.ErrorType.NETWORK_ERROR,
                    e
            );
        } catch (HttpClientErrorException e) {
            log.error("Errore HTTP durante la chiamata ad Alpha Vantage: {} - {}", e.getStatusCode(), e.getMessage(), e);

            if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 403) {
                throw new AlphaVantageException(
                        "Problema di autenticazione con Alpha Vantage. Verifica la tua chiave API.",
                        AlphaVantageException.ErrorType.INVALID_API_KEY,
                        e
                );
            } else if (e.getStatusCode().value() == 429) {
                throw new AlphaVantageException(
                        "Limite di chiamate API Alpha Vantage superato. Riprova più tardi.",
                        AlphaVantageException.ErrorType.API_LIMIT_EXCEEDED,
                        e
                );
            } else if (e.getStatusCode().value() >= 500) {
                throw new AlphaVantageException(
                        "Il servizio Alpha Vantage non è disponibile al momento. Riprova più tardi.",
                        AlphaVantageException.ErrorType.SERVICE_UNAVAILABLE,
                        e
                );
            } else {
                throw new AlphaVantageException(
                        "Errore durante la chiamata ad Alpha Vantage: " + e.getMessage(),
                        AlphaVantageException.ErrorType.UNKNOWN_ERROR,
                        e
                );
            }
        } catch (AlphaVantageException e) {
            // Rilancia le eccezioni AlphaVantageException già generate
            throw e;
        } catch (Exception e) {
            log.error("Errore dettagliato durante il recupero dei dettagli dell'azione {}: {}", symbol, e.getMessage(), e);
            throw new AlphaVantageException(
                    "Errore durante il recupero dei dettagli dell'azione: " + e.getMessage(),
                    AlphaVantageException.ErrorType.UNKNOWN_ERROR,
                    e
            );
        }
    }
}