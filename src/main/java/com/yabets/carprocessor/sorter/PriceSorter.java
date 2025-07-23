package com.yabets.carprocessor.sorter;

import com.yabets.carprocessor.model.Car;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PriceSorter implements CarSorter {
    private final String currency;

    public PriceSorter(String currency) {
        this.currency = currency.toUpperCase();
    }

    @Override
    public List<Car> sort(List<Car> cars) {

        return cars.stream()
                .sorted(Comparator.comparing(
                        car -> car.getPriceMap().getOrDefault(currency, Double.MAX_VALUE),
                        Comparator.reverseOrder()
                ))
                .collect(Collectors.toList());
    }
}
