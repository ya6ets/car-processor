package com.yabets.carprocessor.filter;

import com.yabets.carprocessor.model.Car;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BrandPriceFilter implements CarFilter {

    private static final Logger LOGGER = Logger.getLogger(BrandPriceFilter.class.getName());

    private final String brand;
    private final double minPrice;
    private final double maxPrice;
    private final String currency;

    public BrandPriceFilter(String brand, double minPrice, double maxPrice, String currency) {

        this.brand = brand;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.currency = currency.toUpperCase();
    }

    @Override
    public List<Car> filter(List<Car> cars) {

        LOGGER.info("Filtering cars: brand=" + (brand != null ? brand : "null") + ", currency=" + currency + ", minPrice=" + minPrice + ", maxPrice=" + maxPrice);

        List<Car> filtered = cars.stream()
                .filter(car -> {

                    boolean brandMatch = brand == null || car.brand().equalsIgnoreCase(brand);

                    if (!brandMatch) {

                        LOGGER.fine("Excluding car: model=" + car.model() + ", brand=" + car.brand() + " (brand mismatch)");

                        return false;
                    }

                    Double price = car.getPriceMap().get(currency);

                    boolean priceMatch = price != null && price >= minPrice && price <= maxPrice;

                    if (!priceMatch) {

                        LOGGER.fine("Excluding car: model=" + car.model() + ", brand=" + car.brand() + ", price=" + price + " " + currency + " (price mismatch or missing)");
                    }

                    return priceMatch;

                }).collect(Collectors.toList());

        LOGGER.info("Filtered to " + filtered.size() + " cars");

        return filtered;
    }
}
