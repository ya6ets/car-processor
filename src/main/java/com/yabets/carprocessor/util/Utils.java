package com.yabets.carprocessor.util;

import com.yabets.carprocessor.filter.BrandPriceFilter;
import com.yabets.carprocessor.filter.BrandReleaseDateFilter;
import com.yabets.carprocessor.filter.CarFilter;
import com.yabets.carprocessor.model.Car;
import com.yabets.carprocessor.output.JsonFormatter;
import com.yabets.carprocessor.output.OutputFormatter;
import com.yabets.carprocessor.output.TableFormatter;
import com.yabets.carprocessor.output.XmlFormatter;
import com.yabets.carprocessor.sorter.CarSorter;
import com.yabets.carprocessor.sorter.CurrencyTypeSorter;
import com.yabets.carprocessor.sorter.PriceSorter;
import com.yabets.carprocessor.sorter.ReleaseYearSorter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private static String defaultOutputFormat = "table"; // Default to table output

    public static List<Car> processCommand(String command, List<Car> cars, List<Car> initialCars) {

        String[] parts = command.split("\\s+");

        if (parts.length == 0) {

            printHelp();
            return new ArrayList<>();
        }

        switch (parts[0].toLowerCase()) {
            case "filter":
                if (parts.length < 4) {
                    throw new IllegalArgumentException("Usage: filter <brand-price|brand-date> <params>");
                }
                cars = filterCars(parts, cars);
                outputCars(defaultOutputFormat, cars);
                cars = new ArrayList<>(initialCars); // Reset list after output
                break;
            case "sort":
                if (parts.length < 2) {
                    throw new IllegalArgumentException("Usage: sort <year|price|currency-type> [currency]");
                }
                cars = sortCars(parts, cars);
                outputCars(defaultOutputFormat, cars);
                cars = new ArrayList<>(initialCars); // Reset list after output
                break;
            case "output":
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Usage: output <table|xml|json>");
                }
                defaultOutputFormat = parts[1].toLowerCase();
                outputCars(defaultOutputFormat, cars);
                break;
            case "help":
                printHelp();
                break;
            default:
                throw new IllegalArgumentException("Unknown command: " + parts[0]);
        }

        return cars;
    }

    public static String getBrandFromModel(String model) {

        return switch (model) {
            case "RAV4" -> "Toyota";
            case "Civic" -> "Honda";
            case "F-150" -> "Ford";
            case "Model X" -> "Tesla";
            case "330i" -> "BMW";
            case "Q5" -> "Audi";
            case "Silverado" -> "Chevrolet";
            case "C-Class" -> "Mercedes-Benz";
            case "Rogue" -> "Nissan";
            case "Elantra" -> "Hyundai";
            default -> throw new IllegalArgumentException("Unknown model: " + model);
        };
    }

    private static List<Car> filterCars(String[] parts, List<Car> cars) {

        CarFilter filter;

        if (parts[1].equalsIgnoreCase("brand-price")) {

            if (parts.length != 6) {

                throw new IllegalArgumentException("Usage: filter brand-price <brand> <min-price> <max-price> <currency>");
            }

            String brand = parts[2].equalsIgnoreCase("null") ? null : parts[2];

            double minPrice = Double.parseDouble(parts[3]);
            double maxPrice = Double.parseDouble(parts[4]);
            String currency = parts[5].toUpperCase();

            filter = new BrandPriceFilter(brand, minPrice, maxPrice, currency);

        } else if (parts[1].equalsIgnoreCase("brand-date")) {

            if (parts.length != 5) {

                throw new IllegalArgumentException("Usage: filter brand-date <brand> <start-date> <end-date>");
            }

            String brand = parts[2].equalsIgnoreCase("null") ? null : parts[2];

            try {

                LocalDate startDate = LocalDate.parse(parts[3], DATE_FORMATTER);
                LocalDate endDate = LocalDate.parse(parts[4], DATE_FORMATTER);

                filter = new BrandReleaseDateFilter(brand, startDate, endDate);

            } catch (DateTimeParseException e) {

                throw new IllegalArgumentException("Invalid date format: " + e.getMessage() + ". Expected MM/dd/yyyy");
            }

        } else {

            throw new IllegalArgumentException("Invalid filter type: " + parts[1]);
        }

        return filter.filter(cars);
    }

    private static List<Car> sortCars(String[] parts, List<Car> cars) {

        CarSorter sorter;

        if (parts[1].equalsIgnoreCase("year")) {

            sorter = new ReleaseYearSorter();

        } else if (parts[1].equalsIgnoreCase("price")) {

            if (parts.length != 3) {

                throw new IllegalArgumentException("Usage: sort price <currency>");
            }

            sorter = new PriceSorter(parts[2].toUpperCase());

        } else if (parts[1].equalsIgnoreCase("currency-type")) {

            sorter = new CurrencyTypeSorter();

        } else {

            throw new IllegalArgumentException("Invalid sort type: " + parts[1]);
        }

        return sorter.sort(cars);
    }

    private static void outputCars(String format, List<Car> cars) {

        OutputFormatter formatter = switch (format.toLowerCase()) {
            case "table" -> new TableFormatter();
            case "xml" -> new XmlFormatter();
            case "json" -> new JsonFormatter();
            default -> throw new IllegalArgumentException("Invalid output format: " + format);
        };

        System.out.println(formatter.format(cars));
    }

    private static void printHelp() {

        System.out.println("""
            Commands:
              filter brand-price <brand|null> <min-price> <max-price> <currency> - Filter by brand and price, outputs table
              filter brand-date <brand|null> <start-date> <end-date> - Filter by brand and release date (MM/dd/yyyy), outputs table
              sort year - Sort by release year (latest to oldest), outputs table
              sort price <currency> - Sort by price (highest to lowest), outputs table
              sort currency-type - Sort SUVs (EUR), Sedans (JPY), Trucks (USD), outputs table
              output <table|xml|json> - Set output format (default: table)
              help - Show this help message
              exit - Exit the application
            Note: Car list resets to initial state after each filter or sort command.
            """);
    }
}
