package com.yabets.carprocessor.output;

import com.yabets.carprocessor.model.Car;

import java.util.List;
import java.util.Map;

public class TableFormatter implements OutputFormatter {

    @Override
    public String format(List<Car> cars) {

        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%-10s %-15s %-10s %-10s %-10s %-10s %-12s%n",
                "Type", "Model", "Brand", "USD", "EUR", "GBP", "JPY", "Release Date"));

        sb.append("-".repeat(80)).append("\n");

        for (Car car : cars) {

            Map<String, Double> priceMap = car.getPriceMap();

            sb.append(String.format("%-10s %-15s %-10s %-10.2f %-10.2f %-10.2f %-10.2f %-12s%n",
                    car.type(), car.model(), car.brand(),
                    priceMap.getOrDefault("USD", 0.0),
                    priceMap.getOrDefault("EUR", 0.0),
                    priceMap.getOrDefault("GBP", 0.0),
                    priceMap.getOrDefault("JPY", 0.0),
                    car.releaseDate() != null ? car.releaseDate().toString() : ""));
        }

        return sb.toString();
    }
}
