package com.richieoscar.currencyconverter.controller;

import com.richieoscar.currencyconverter.dto.ConvertRequest;
import com.richieoscar.currencyconverter.dto.DefaultApiResponse;
import com.richieoscar.currencyconverter.service.Converter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/converter")
@RequiredArgsConstructor
public class CurrencyConverterController {

    private final Converter converter;

    @GetMapping("/currencies")
    public ResponseEntity<DefaultApiResponse> getCurrencies(@RequestParam("currencyCode") String code) {
        log.info("CurrencyConverterController::getCurrencies");
        return ResponseEntity.ok(converter.getCurrencies(code));
    }

    @PostMapping(value = "/convert")
    public void convert(@RequestBody ConvertRequest request, HttpServletResponse response) {
        log.info("CurrencyConverterController::convert()");
        converter.convert(request, response);
    }
}
