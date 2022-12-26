package com.richieoscar.currencyconverter.service;

import com.richieoscar.currencyconverter.dto.ConvertRequest;
import com.richieoscar.currencyconverter.dto.DefaultApiResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface Converter {

    void convert(ConvertRequest request, HttpServletResponse response);

   DefaultApiResponse getCurrencies(String seedId);

}
