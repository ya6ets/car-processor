package com.yabets.carprocessor.cli;

import com.yabets.carprocessor.model.BrandInfo;
import com.yabets.carprocessor.model.Car;
import com.yabets.carprocessor.parser.BrandCsvParser;
import com.yabets.carprocessor.parser.CarXmlParser;
import com.yabets.carprocessor.util.Utils;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CarProcessorCli {

    private static final Logger LOGGER = Logger.getLogger(CarProcessorCli.class.getName());

    private final CarXmlParser xmlParser = new CarXmlParser();
    private final BrandCsvParser csvParser = new BrandCsvParser();

    private List<Car> cars = new ArrayList<>();
    private List<Car> initialCars = new ArrayList<>(); // Store initial car list
    private final LineReader reader;
    private final boolean useJLine;

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

                    String brand = Utils.getBrandFromModel(car.model());

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

                cars = Utils.processCommand(command, cars, initialCars);

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

                cars = Utils.processCommand(command, cars, initialCars);

            } catch (Exception e) {

                System.err.println("Error: " + e.getMessage());
                LOGGER.severe("Command error: " + e.getMessage());
            }

            System.out.print("car-processor> ");
        }
    }
}
