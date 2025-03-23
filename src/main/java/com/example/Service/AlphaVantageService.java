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

}
