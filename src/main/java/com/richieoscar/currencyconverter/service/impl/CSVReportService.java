package com.richieoscar.currencyconverter.service.impl;

import com.richieoscar.currencyconverter.exception.FileReportException;
import com.richieoscar.currencyconverter.dto.CsvReportDto;
import com.richieoscar.currencyconverter.service.FileReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CSVReportService implements FileReportService<List<CsvReportDto>, HttpServletResponse> {
    @Override
    public void exportCSV(List<CsvReportDto> data, HttpServletResponse response) {
        log.info("Downloading CSV File....");
        String[] headings = getHeadings();
        String[] classPropertyName = Arrays.copyOf(headings, headings.length);
        try {
            setResponseHeader(response, "text/csv", "conversion", ".csv");
            // write to csv file //
            ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
            csvWriter.writeHeader(headings);
            for (CsvReportDto csvReportDto : data) {
                csvWriter.write(csvReportDto, classPropertyName);
            }
            csvWriter.close();
            log.info("Download Completed, CSV File Created");
        } catch (Exception e) {
            log.error("Error Occurred while creating CSV {}", e.getMessage());
            throw new FileReportException("Error Processing Download");
        }
    }


    private String[] getHeadings() {
        CsvReportDto csvReports = CsvReportDto.builder().build();
        Field[] declaredFields = csvReports.getClass().getDeclaredFields();
        List<String> fieldNames = Arrays.stream(declaredFields).map(Field::getName).collect(Collectors.toList());
        String[] headings = new String[declaredFields.length];
        for (int i = 0; i < declaredFields.length; i++) {
            headings[i] = fieldNames.get(i).toUpperCase();
        }
        return headings;
    }

    private void setResponseHeader(HttpServletResponse response, String contentType, String prefix, String extension) {
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String fileName = prefix + timeStamp + extension;
        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        log.info("FileName => {}", fileName);
    }
}
