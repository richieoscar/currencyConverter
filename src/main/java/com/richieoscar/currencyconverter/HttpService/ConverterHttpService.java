package com.richieoscar.currencyconverter.HttpService;

import com.richieoscar.currencyconverter.exception.CurrencyConverterException;
import com.richieoscar.currencyconverter.dto.ConverterResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Service
public class ConverterHttpService {

    private final RestTemplate restTemplate;

    private static final String BASE_URL = "https://api-coding-challenge.neofinancial.com/currency-conversion?seed=41046";

    public ConverterResponse[] getCurrencies() {
        log.info("Sending Request to getCurrencies");
        ResponseEntity<ConverterResponse[]> response = null;
        log.info("Service Url {}", BASE_URL);
        try {
            response = restTemplate.getForEntity(BASE_URL, ConverterResponse[].class);
            log.info("ApiResponse {}", response.getBody());
        } catch (Exception e) {
            log.error("Error Occurred while fetching Currencies");
            throw new CurrencyConverterException("Error Processing Request");
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            return null;
        }
    }

}
