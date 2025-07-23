package com.yabets.carprocessor.cli;

import com.yabets.carprocessor.filter.BrandPriceFilter;
import com.yabets.carprocessor.filter.BrandReleaseDateFilter;
import com.yabets.carprocessor.filter.CarFilter;
import com.yabets.carprocessor.model.BrandInfo;
import com.yabets.carprocessor.model.Car;
import com.yabets.carprocessor.output.JsonFormatter;
import com.yabets.carprocessor.output.OutputFormatter;
import com.yabets.carprocessor.output.TableFormatter;
import com.yabets.carprocessor.output.XmlFormatter;
import com.yabets.carprocessor.parser.BrandCsvParser;
import com.yabets.carprocessor.parser.CarXmlParser;
import com.yabets.carprocessor.sorter.CarSorter;
import com.yabets.carprocessor.sorter.CurrencyTypeSorter;
import com.yabets.carprocessor.sorter.PriceSorter;
import com.yabets.carprocessor.sorter.ReleaseYearSorter;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CarProcessorCli {

    private static final Logger LOGGER = Logger.getLogger(CarProcessorCli.class.getName());

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private final CarXmlParser xmlParser = new CarXmlParser();
    private final BrandCsvParser csvParser = new BrandCsvParser();

    private List<Car> cars = new ArrayList<>();
    private List<Car> initialCars = new ArrayList<>(); // Store initial car list
    private final LineReader reader;
    private final boolean useJLine;
    private String defaultOutputFormat = "table"; // Default to table output

    public CarProcessorCli() throws Exception {

        LineReader tempReader = null;
        boolean jlineSuccess = false;

        try {

            Terminal terminal = TerminalBuilder.builder()
                    .system(System.console() != null)
                    .dumb(true)
                    .build();

            tempReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .build();

            jlineSuccess = true;

            LOGGER.fine("Initialized JLine terminal successfully");

        } catch (Exception e) {

            LOGGER.warning("Failed to initialize JLine terminal, falling back to Scanner: " + e.getMessage());
        }

        reader = tempReader;
        useJLine = jlineSuccess;

        // Automatically load files from resources
        try {

            loadFilesFromResources();

        } catch (Exception e) {

            LOGGER.severe("Failed to load resources: " + e.getMessage());

            throw new Exception("Failed to load cars.xml or brands.csv: " + e.getMessage());
        }
    }

    private void loadFilesFromResources() throws Exception {

        long startTime = System.currentTimeMillis();

        // Load cars.xml from classpath
        InputStream xmlStream = getClass().getClassLoader().getResourceAsStream("cars.xml");

        if (xmlStream == null) {

            throw new IllegalArgumentException("Resource not found: cars.xml");
        }

        List<Car> rawCars = xmlParser.parse(xmlStream);

        // Load brands.csv from classpath
        InputStream csvStream = getClass().getClassLoader().getResourceAsStream("brands.csv");

        if (csvStream == null) {

            throw new IllegalArgumentException("Resource not found: brands.csv");
        }

        List<BrandInfo> brands = csvParser.parse(csvStream);

        Map<String, LocalDate> brandDateMap = brands.stream()
                .collect(Collectors.toMap(BrandInfo::brand, BrandInfo::releaseDate));

        cars = rawCars.stream()
                .map(car -> {

                    String brand = getBrandFromModel(car.model(), brands);

                    return new Car(car.type(), car.model(), car.prices(), brand, brandDateMap.getOrDefault(brand, null));

                }).collect(Collectors.toList());

        initialCars = new ArrayList<>(cars); // Store copy of initial list

        // Log loaded cars for debugging
        for (Car car : cars) {

            LOGGER.fine("Loaded car: model=" + car.model() + ", brand=" + car.brand() + ", prices=" + car.getPriceMap() + ", releaseDate=" + car.releaseDate());
        }

        long duration = System.currentTimeMillis() - startTime;

        LOGGER.fine("Loaded " + cars.size() + " cars in " + duration + " ms");
    }

    public void run() {

        if (useJLine) {

            runJLineCli();

        } else {

            runScannerCli();
        }
    }

    private void runJLineCli() {

        while (true) {

            String command = reader.readLine("car-processor> ").trim();

            if (command.equalsIgnoreCase("exit")) { break; }

            try {

                processCommand(command);

            } catch (Exception e) {

                System.err.println("Error: " + e.getMessage());
                LOGGER.severe("Command error: " + e.getMessage());
            }
        }
    }

    private void runScannerCli() {

        Scanner scanner = new Scanner(new BufferedReader(new InputStreamReader(System.in)));

        System.out.print("car-processor> ");

        while (scanner.hasNextLine()) {

            String command = scanner.nextLine().trim();

            if (command.equalsIgnoreCase("exit")) { break; }

            try {

                processCommand(command);

            } catch (Exception e) {

                System.err.println("Error: " + e.getMessage());
                LOGGER.severe("Command error: " + e.getMessage());
            }

            System.out.print("car-processor> ");
        }
    }

    private void processCommand(String command) throws Exception {

        String[] parts = command.split("\\s+");

        if (parts.length == 0) {

            printHelp();
            return;
        }

        switch (parts[0].toLowerCase()) {
            case "filter":
                if (parts.length < 4) {
                    throw new IllegalArgumentException("Usage: filter <brand-price|brand-date> <params>");
                }
                filterCars(parts);
                outputCars(defaultOutputFormat);
                cars = new ArrayList<>(initialCars); // Reset list after output
                break;
            case "sort":
                if (parts.length < 2) {
                    throw new IllegalArgumentException("Usage: sort <year|price|currency-type> [currency]");
                }
                sortCars(parts);
                outputCars(defaultOutputFormat);
                cars = new ArrayList<>(initialCars); // Reset list after output
                break;
            case "output":
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Usage: output <table|xml|json>");
                }
                defaultOutputFormat = parts[1].toLowerCase();
                outputCars(defaultOutputFormat);
                break;
            case "help":
                printHelp();
                break;
            default:
                throw new IllegalArgumentException("Unknown command: " + parts[0]);
        }
    }

    private String getBrandFromModel(String model, List<BrandInfo> brands) {

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

    private void filterCars(String[] parts) {

        long startTime = System.currentTimeMillis();
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

        cars = filter.filter(cars);
        long duration = System.currentTimeMillis() - startTime;

        LOGGER.info("Filtered to " + cars.size() + " cars in " + duration + " ms");
        System.out.println("Filtered to " + cars.size() + " cars.");
    }

    private void sortCars(String[] parts) {

        long startTime = System.currentTimeMillis();
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

        cars = sorter.sort(cars);
        long duration = System.currentTimeMillis() - startTime;

        LOGGER.info("Sorted " + cars.size() + " cars in " + duration + " ms");
        System.out.println("Sorted " + cars.size() + " cars.");
    }

    private void outputCars(String format) {

        long startTime = System.currentTimeMillis();

        OutputFormatter formatter = switch (format.toLowerCase()) {
            case "table" -> new TableFormatter();
            case "xml" -> new XmlFormatter();
            case "json" -> new JsonFormatter();
            default -> throw new IllegalArgumentException("Invalid output format: " + format);
        };

        System.out.println(formatter.format(cars));

        long duration = System.currentTimeMillis() - startTime;

        LOGGER.info("Output in " + format + " format in " + duration + " ms");
    }

    private void printHelp() {

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
