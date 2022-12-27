package com.richieoscar.currencyconverter.exception;

import com.richieoscar.currencyconverter.dto.DefaultApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AppExceptionHandler {

    @ExceptionHandler(CurrencyConverterException.class)
    public DefaultApiResponse handleConvertException(CurrencyConverterException e){
        DefaultApiResponse defaultApiResponse = new DefaultApiResponse();
        defaultApiResponse.setMessage(e.getMessage());
        defaultApiResponse.setStatus("success");
        return defaultApiResponse;
    }
}
