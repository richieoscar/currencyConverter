package com.richieoscar.currencyconverter.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ConvertRequest {
    private String fromCurrencyCode;
    private String toCurrencyCode;
    private BigDecimal amount;
}
