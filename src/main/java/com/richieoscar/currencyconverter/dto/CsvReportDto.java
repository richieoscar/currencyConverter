package com.richieoscar.currencyconverter.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CsvReportDto {
    private String currencyCode;
    private String country;
    private BigDecimal amount;
    private String path;
}
