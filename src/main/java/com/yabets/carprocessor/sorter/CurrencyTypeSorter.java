package com.yabets.carprocessor.sorter;

import com.yabets.carprocessor.model.Car;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CurrencyTypeSorter implements CarSorter {

    @Override
    public List<Car> sort(List<Car> cars) {

        return cars.stream()
                .sorted(Comparator.comparing(car -> {

                    String currency = switch (car.type()) {

                        case "SUV" -> "EUR";
                        case "Sedan" -> "JPY";
                        case "Truck" -> "USD";
                        default -> "USD";
                    };

                    return car.getPriceMap().getOrDefault(currency, 0.0);

                }, Comparator.reverseOrder())).collect(Collectors.toList());
    }
}