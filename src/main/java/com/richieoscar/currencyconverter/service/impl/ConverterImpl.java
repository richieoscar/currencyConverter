package com.richieoscar.currencyconverter.service.impl;

import com.richieoscar.currencyconverter.http.ConverterHttpService;
import com.richieoscar.currencyconverter.dto.ConvertRequest;
import com.richieoscar.currencyconverter.dto.ConverterResponse;
import com.richieoscar.currencyconverter.dto.CsvReportDto;
import com.richieoscar.currencyconverter.dto.DefaultApiResponse;
import com.richieoscar.currencyconverter.exception.CurrencyConverterException;
import com.richieoscar.currencyconverter.service.Converter;
import com.richieoscar.currencyconverter.service.FileReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

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
        Map<String, ArrayList<ConverterResponse>> dataSet = new HashMap<>();
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
        List<ConverterResponse> pairMatchingRequestToCurrencyCode = Arrays.stream(currencies)
                .filter(converterResponse -> request.getToCurrencyCode().equals(converterResponse.getToCurrencyCode())).collect(toList());


        dataSet.put("matchingPair", new ArrayList<>(conversionPair));
        dataSet.put("toCurrencyCodePair", new ArrayList<>(pairMatchingRequestToCurrencyCode));
        dataSet.put("currency", new ArrayList<>(Arrays.asList(currencies)));

        //DO THE CONVERSION
        List<ConverterResponse> convertedCurrency = doConversion(dataSet, request.getAmount());
        List<CsvReportDto> conversionFileList = convertedCurrency.stream().map(converterResponse -> CsvReportDto.builder().
                amount(converterResponse.getConvertedAmount())
                .currencyCode(converterResponse.getToCurrencyCode())
                .country(converterResponse.getCountry())
                .path(converterResponse.getPath())
                .build()).collect(toList());
        fileReportService.exportCSV(conversionFileList, response);
    }

    private List<ConverterResponse> doConversion(Map<String, ArrayList<ConverterResponse>> data, BigDecimal amount) {
        ArrayList<ConverterResponse> matchingPair = data.get("matchingPair");
        ArrayList<ConverterResponse> toCurrencyCodePair = data.get("toCurrencyCodePair");
        ArrayList<ConverterResponse> currency = data.get("currency");
        if (!matchingPair.isEmpty()) {
            ConverterResponse pair = matchingPair.get(0);
            BigDecimal exchangeRate = BigDecimal.valueOf(Double.parseDouble(pair.getExchangeRate()));
            BigDecimal convertedAmount = amount.multiply(exchangeRate);
            pair.setConvertedAmount(convertedAmount);
            pair.setPath(String.format("%s | %s", pair.getFromCurrencyCode(), pair.getToCurrencyCode()));
            getCountry(pair);
            return matchingPair;
        } else if (!toCurrencyCodePair.isEmpty()) {
            log.info("ToCurrencyPair {}", toCurrencyCodePair);
            Set<ConverterResponse> cadSet = new HashSet<>();
            for (ConverterResponse res : currency) {
                for (ConverterResponse toPair : toCurrencyCodePair) {
                    if (res.getFromCurrencyCode().equals("CAD") && res.getToCurrencyCode()
                            .equals(toPair.getFromCurrencyCode())) {
                        cadSet.add(res);
                    }
                }
            }
            if (cadSet.isEmpty()) throw new CurrencyConverterException("No Conversion Available");

            log.info("CadSet {}", cadSet);
            Optional<ConverterResponse> maxValue = cadSet.stream().map(converterResponse -> {
                converterResponse.setConvertedAmount(amount.multiply(BigDecimal.valueOf(Double.parseDouble(converterResponse.getExchangeRate()))));
                return converterResponse;
            }).max(Comparator.comparing(ConverterResponse::getConvertedAmount));

            //Max is the highest value from conversion of CAD to Other currencies
            if (maxValue.isPresent()) {
                ConverterResponse bestRatePair = maxValue.get();
                log.info("Best rate pair {}", bestRatePair);
                return toCurrencyCodePair.stream().filter(converterResponse -> converterResponse.getFromCurrencyCode().equals(bestRatePair.getToCurrencyCode()))
                        .map(converterResponse -> {
                            BigDecimal finalConvertedAmount = bestRatePair.getConvertedAmount().multiply(BigDecimal.valueOf(Double.parseDouble(converterResponse.getExchangeRate())));
                            converterResponse.setConvertedAmount(finalConvertedAmount.setScale(2, RoundingMode.HALF_EVEN));
                            converterResponse.setPath(String.format("%s | %s | %s", bestRatePair.getFromCurrencyCode(), bestRatePair.getToCurrencyCode(), converterResponse.getToCurrencyCode()));
                            return getCountry(converterResponse);
                        }).collect(toList());
            }

        } else return Collections.emptyList();

        return Collections.emptyList();
    }

    private ConverterResponse getCountry(ConverterResponse converterResponse) {
        String[] s = converterResponse.getFromCurrencyName().split(" ");
        if (s.length == 1) {
            converterResponse.setCountry(s[0]);
        } else {
            converterResponse.setCountry(s[0]);
        }
        return converterResponse;
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

}
