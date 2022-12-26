package com.richieoscar.currencyconverter.dto;

import lombok.Data;

@Data
public class DefaultApiResponse<T> {
    private String status;
    private String message;
    private T data;
}
