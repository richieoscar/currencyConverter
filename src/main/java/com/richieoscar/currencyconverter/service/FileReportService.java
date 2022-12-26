package com.richieoscar.currencyconverter.service;

public interface FileReportService<T, R> {

    void exportCSV(T data,R writer);
}
