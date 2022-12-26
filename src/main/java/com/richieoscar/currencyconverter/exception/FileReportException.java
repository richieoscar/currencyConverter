package com.richieoscar.currencyconverter.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@ResponseStatus(HttpStatus.PROCESSING)
public class FileReportException extends RuntimeException {
    public FileReportException(String message) {
        super(message);
    }
}
