package com.richieoscar.currencyconverter.service.impl;

import com.richieoscar.currencyconverter.HttpService.ConverterHttpService;
import com.richieoscar.currencyconverter.dto.ConvertRequest;
import com.richieoscar.currencyconverter.dto.ConverterResponse;
import com.richieoscar.currencyconverter.dto.CsvReportDto;
import com.richieoscar.currencyconverter.dto.DefaultApiResponse;
import com.richieoscar.currencyconverter.dto.enums.CurrencyCode;
import com.richieoscar.currencyconverter.exception.CurrencyConverterException;
import com.richieoscar.currencyconverter.service.Converter;
import com.richieoscar.currencyconverter.service.FileReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.richieoscar.currencyconverter.dto.Constant.*;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConverterImpl implements Converter {

    private final ConverterHttpService converterHttpService;

    private final FileReportService fileReportService;


    @Override
    public void convert(ConvertRequest request, HttpServletResponse response) {
        //GET CURRENCY RATES FORM API
        ConverterResponse[] currencies = converterHttpService.getCurrencies();
        if (currencies == null) {
            throw new CurrencyConverterException("Could not Fetch Conversion pair");
        }
        //FILTERS THE CURRENCIES PAIR FROM API TO MATCH CONVERSION REQUEST
        List<ConverterResponse> conversionPair = Arrays.stream(currencies)
                .filter(converterResponse ->
                        converterResponse.getFromCurrencyCode().equals(request.getFromCurrencyCode())
                                && converterResponse.getToCurrencyCode().equals(request.getToCurrencyCode())
                ).toList();
        if (conversionPair.isEmpty()) {
            throw new CurrencyConverterException("Unable to Convert Currency Pair");
        }
        //DO THE CONVERSION
        List<ConverterResponse> convertedCurrency = doConversion(conversionPair.get(0), request.getAmount());
        List<CsvReportDto> conversionFileList = convertedCurrency.stream().map(converterResponse -> CsvReportDto.builder().
                amount(converterResponse.getConvertedAmount())
                .currencyCode(converterResponse.getToCurrencyCode())
                .country(converterResponse.getCountry())
                .path(converterResponse.getPath())
                .build()).collect(toList());
        fileReportService.exportCSV(conversionFileList, response);
    }

    @Override
    public DefaultApiResponse getCurrencies(String currecyCode) {
        log.info("ConverterImpl::getCurrencies");
        DefaultApiResponse defaultApiResponse = new DefaultApiResponse();
        ConverterResponse[] currencies = converterHttpService.getCurrencies();
        if (currencies != null) {
            List<ConverterResponse> converterResponses = Arrays.stream(currencies).
                    filter(converterResponse -> converterResponse.getFromCurrencyCode().equals(currecyCode)).collect(toList());

            defaultApiResponse.setData(converterResponses);
            defaultApiResponse.setStatus("00");
            defaultApiResponse.setMessage("Currency Code Conversion Pair Retrieved");
        } else {
            defaultApiResponse.setStatus("99");
            defaultApiResponse.setMessage("Could not Fetch Currencies");
        }
        log.info("Currency Retrieved");
        return defaultApiResponse;
    }


    private List<ConverterResponse> doConversion(ConverterResponse currency, BigDecimal amount) {
        List<ConverterResponse> response = new ArrayList<>();
        CurrencyCode currencyCode = CurrencyCode.valueOf(currency.getToCurrencyCode());
        switch (currencyCode) {
            case HKD -> {
                log.info("Converting from {} to {}", currency.getFromCurrencyCode(), currency.getToCurrencyCode());
                //using GPB as unitary currency for Best rate
                //1CAD = 0.61GBP
                BigDecimal cadToGbp = amount.multiply(BigDecimal.valueOf(GBP_RATE));
                BigDecimal gbpToHkd = cadToGbp.multiply(BigDecimal.valueOf(GBP_HKD_RATE));
                currency.setConvertedAmount(gbpToHkd.setScale(2, RoundingMode.HALF_EVEN));
                currency.setPath("CAD => GPB => HKD");
                currency.setCountry("HONG KONG");
                response.add(currency);

            }
            case EUR -> {
                log.info("Converting from {} to {}", currency.getFromCurrencyCode(), currency.getToCurrencyCode());
                //using GPB as unitary currency for Best rate
                //1CAD = 0.61GBP
                BigDecimal cadToGbp = amount.multiply(BigDecimal.valueOf(GBP_RATE));
                BigDecimal gbpToEur = cadToGbp.multiply(BigDecimal.valueOf(GBP_EUR_RATE));
                currency.setConvertedAmount(gbpToEur.setScale(2, RoundingMode.HALF_EVEN));
                currency.setPath("CAD => GPB => EUR");
                currency.setCountry("Europe");
                response.add(currency);
            }
            case BTC -> {
                log.info("Converting from {} to {}", currency.getFromCurrencyCode(), currency.getToCurrencyCode());
                //using USD as unitary currency for Best rate
                //1CAD = 0.73USD
                BigDecimal cadToUsd = amount.multiply(BigDecimal.valueOf(USD_RATE));
                BigDecimal usdToBtc = cadToUsd.multiply(BigDecimal.valueOf(USD_BTC_RATE));
                currency.setConvertedAmount(usdToBtc.setScale(2, RoundingMode.HALF_EVEN));
                currency.setPath("CAD => USD => BTC");
                currency.setCountry("BITCOIN");
                response.add(currency);
            }

            case BRL -> {
                log.info("Converting from {} to {}", currency.getFromCurrencyCode(), currency.getToCurrencyCode());
                //using USD as unitary currency for Best rate
                //1CAD = 0.73USD
                BigDecimal cadToUsd = amount.multiply(BigDecimal.valueOf(USD_RATE));
                BigDecimal usdToBrl = cadToUsd.multiply(BigDecimal.valueOf(USD_BRL_RATE));
                currency.setConvertedAmount(usdToBrl.setScale(2, RoundingMode.HALF_EVEN));
                currency.setCountry("BRAZIL");
                currency.setPath("CAD => USD => BRL");
                response.add(currency);
            }
            case USD -> {
                log.info("Converting from {} to {}", currency.getFromCurrencyCode(), currency.getToCurrencyCode());
                //using GPB as unitary currency for Best rate
                //1CAD = 0.61GBP
                BigDecimal cadToGpb = amount.multiply(BigDecimal.valueOf(GBP_RATE));
                BigDecimal gpbToUsd = cadToGpb.multiply(BigDecimal.valueOf(GBP_USD_RATE));
                currency.setConvertedAmount(gpbToUsd.setScale(2, RoundingMode.HALF_EVEN));
                currency.setCountry("UNITED STATES OF AMERICA");
                currency.setPath("CAD => GBP => USD");
                response.add(currency);
            }

            default -> log.info("No match found");

        }
        return response;
    }
}
