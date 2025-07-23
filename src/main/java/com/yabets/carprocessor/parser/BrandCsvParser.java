package com.yabets.carprocessor.parser;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.yabets.carprocessor.model.BrandInfo;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class BrandCsvParser {

    private static final Logger LOGGER = Logger.getLogger(BrandCsvParser.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public List<BrandInfo> parse(InputStream csvStream) throws IOException, CsvValidationException {

        List<BrandInfo> brands = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(csvStream))) {

            // Skip header
            String[] header = reader.readNext();
            if (header == null || header.length < 2 || !header[0].trim().equalsIgnoreCase("Brand") || !header[1].trim().equalsIgnoreCase("ReleaseDate")) {

                LOGGER.severe("Invalid CSV header: Expected 'Brand,ReleaseDate', found: " + (header != null ? String.join(",", header) : "null"));
                throw new IOException("Invalid CSV header: Expected 'Brand,ReleaseDate'");
            }

            String[] fields;
            int lineNumber = 1;

            while ((fields = reader.readNext()) != null) {

                lineNumber++;

                if (fields.length < 2 || fields[0].trim().isEmpty()) {

                    LOGGER.warning("Skipping invalid row at line " + lineNumber + ": " + String.join(",", fields));
                    continue;
                }

                try {

                    String brand = fields[0].trim();
                    String dateStr = fields[1].trim();

                    if (dateStr.isEmpty()) {

                        LOGGER.warning("Skipping row at line " + lineNumber + ": Empty releaseDate");
                        continue;
                    }

                    LocalDate releaseDate = LocalDate.parse(dateStr, DATE_FORMATTER);
                    brands.add(new BrandInfo(brand, releaseDate));

                } catch (DateTimeParseException e) {

                    LOGGER.warning("Skipping row at line " + lineNumber + ": Invalid date format '" + fields[1] + "', expected MM/dd/yyyy");

                } catch (Exception e) {

                    LOGGER.warning("Skipping row at line " + lineNumber + ": " + e.getMessage());
                }
            }
        }

        if (brands.isEmpty()) {

            LOGGER.severe("No valid brand data parsed from CSV");
            throw new IOException("No valid brand data parsed from CSV");
        }

        return brands;
    }
}
