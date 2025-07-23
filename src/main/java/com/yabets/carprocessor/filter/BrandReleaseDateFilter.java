package com.yabets.carprocessor.filter;

import com.yabets.carprocessor.model.Car;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BrandReleaseDateFilter implements CarFilter {

    private static final Logger LOGGER = Logger.getLogger(BrandReleaseDateFilter.class.getName());

    private final String brand;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public BrandReleaseDateFilter(String brand, LocalDate startDate, LocalDate endDate) {

        this.brand = brand;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public List<Car> filter(List<Car> cars) {

        LOGGER.info("Filtering cars: brand=" + (brand != null ? brand : "null") + ", startDate=" + startDate + ", endDate=" + endDate);

        List<Car> filtered = cars.stream()
                .filter(car -> {

                    boolean brandMatch = brand == null || car.brand().equalsIgnoreCase(brand);
                    if (!brandMatch) {

                        LOGGER.fine("Excluding car: model=" + car.model() + ", brand=" + car.brand() + " (brand mismatch)");

                        return false;
                    }

                    boolean dateMatch = car.releaseDate() != null &&
                            !car.releaseDate().isBefore(startDate) &&
                            !car.releaseDate().isAfter(endDate);

                    if (!dateMatch) {

                        LOGGER.fine("Excluding car: model=" + car.model() + ", brand=" + car.brand() + ", releaseDate=" + car.releaseDate() + " (date mismatch)");
                    }

                    return dateMatch;

                }).collect(Collectors.toList());

        LOGGER.info("Filtered to " + filtered.size() + " cars");

        return filtered;
    }
}
