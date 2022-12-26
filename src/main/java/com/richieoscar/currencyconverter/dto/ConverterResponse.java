package com.richieoscar.currencyconverter.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ConverterResponse {
    private String exchangeRate;
    private String fromCurrencyCode;
    private String fromCurrencyName;
    private String toCurrencyCode;
    private String toCurrencyName;
    private BigDecimal convertedAmount;
    private String path;
    private String country;
}

