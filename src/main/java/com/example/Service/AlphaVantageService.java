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

    public List<Stock> searchStock(String query){

        try{
            String url = baseUrl + "?function=SYMBOL_SEARCH&keywords=" + query + "&apikey=" + apiKey;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode matchesNode = rootNode.path("bestMatches");

            List<Stock> stocks = new ArrayList<>();

            for (JsonNode matchNode : matchesNode){
                Stock stock = new Stock();
                stock.setSymbol(matchNode.path("1. symbol").asText());
                stock.setName(matchNode.path("2. name").asText());
                stock.setExchangeName(matchNode.path("4. region").asText());
                stock.setCurrency(matchNode.path("8. currency").asText());

                stocks.add(stock);
            }

            return stocks;

        } catch (Exception e) {
            log.error("Errore durante la ricerca delle azioni: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public Stock getStockDetails(String symbol) {
        try {
            String quoteUrl = baseUrl + "?function=GLOBAL_QUOTE&symbol=" + symbol + "&apikey=" + apiKey;
            ResponseEntity<String> quoteResponse = restTemplate.getForEntity(quoteUrl, String.class);

            JsonNode quoteNode = objectMapper.readTree(quoteResponse.getBody()).path("Global Quote");

            if (quoteNode.isEmpty()) {
                throw new RuntimeException("Nessun dato trovato per il simbolo: " + symbol);
            }

            String overviewUrl = baseUrl + "?function=OVERVIEW&symbol=" + symbol + "&apikey=" + apiKey;
            ResponseEntity<String> overviewResponse = restTemplate.getForEntity(overviewUrl, String.class);

            JsonNode overviewNode = objectMapper.readTree(overviewResponse.getBody());

            Stock stock = new Stock();
            stock.setSymbol(symbol);
            stock.setName(overviewNode.path("Name").asText(symbol));
            stock.setExchangeName(overviewNode.path("Exchange").asText());
            stock.setCurrency(overviewNode.path("Currency").asText("USD"));


            String priceStr = quoteNode.path("05. price").asText("0");
            stock.setCurrentPrice(new BigDecimal(priceStr));

            String changeStr = quoteNode.path("09. change").asText("0");
            String changePercentStr = quoteNode.path("10. change percent").asText("0%").replace("%", "");

            stock.setDailyChange(new BigDecimal(changeStr));
            stock.setDailyChangePercent(new BigDecimal(changePercentStr));

            String volumeStr = quoteNode.path("06. volume").asText("0");
            stock.setVolume(new BigDecimal(volumeStr));

            String marketCapStr = overviewNode.path("MarketCapitalization").asText("0");
            stock.setMarketCap(new BigDecimal(marketCapStr));

            return stock;
        } catch (Exception e) {
            log.error("Errore durante il recupero dei dettagli dell'azione: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero dei dettagli dell'azione", e);
        }
    }


}
