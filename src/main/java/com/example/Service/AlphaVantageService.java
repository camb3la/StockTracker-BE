package com.example.Service;

import com.example.Model.Stock;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


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
                throw new RuntimeException("Risposta vuota dall'API Alpha Vantage");
            }

            if (responseBody.trim().startsWith("<")) {
                log.error("Ricevuta risposta HTML invece di JSON: {}", responseBody.substring(0, Math.min(responseBody.length(), 100)));
                throw new RuntimeException("Risposta non valida dall'API Alpha Vantage. Verifica la tua chiave API.");
            }

            log.debug("Risposta GLOBAL_QUOTE: {}", responseBody);
            JsonNode quoteNode = objectMapper.readTree(responseBody).path("Global Quote");

            if (quoteNode.isEmpty()) {
                log.warn("Nessun dato 'Global Quote' trovato per il simbolo: {}", symbol);
                throw new RuntimeException("Nessun dato trovato per il simbolo: " + symbol);
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
                throw new RuntimeException("Risposta vuota dall'API Alpha Vantage per i dettagli dell'azienda");
            }

            if (overviewBody.trim().startsWith("<")) {
                log.warn("Ricevuta risposta HTML per i dettagli dell'azienda. Procedendo con dati parziali.");
                // Continua comunque con i dati della quotazione che abbiamo
            } else {
                log.debug("Risposta OVERVIEW: {}", overviewBody);
            }

            // Crea l'oggetto Stock con i dati disponibili
            Stock stock = new Stock();
            stock.setSymbol(symbol);

            // Prova a leggere i dati dell'overview, ma non fallire se non sono disponibili
            JsonNode overviewNode;
            try {
                overviewNode = objectMapper.readTree(overviewBody);
                stock.setName(overviewNode.path("Name").asText(symbol));
                stock.setExchangeName(overviewNode.path("Exchange").asText(""));
                stock.setCurrency(overviewNode.path("Currency").asText("USD"));

                String marketCapStr = overviewNode.path("MarketCapitalization").asText("0");
                stock.setMarketCap(new BigDecimal(marketCapStr));
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
        } catch (Exception e) {
            log.error("Errore durante il recupero dei dettagli dell'azione: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero dei dettagli dell'azione: " + e.getMessage(), e);
        }
    }


}
