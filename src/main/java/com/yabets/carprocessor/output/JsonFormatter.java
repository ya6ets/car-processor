package com.yabets.carprocessor.output;

import com.yabets.carprocessor.model.Car;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonFormatter implements OutputFormatter {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String format(List<Car> cars) {

        try {

            List<Map<String, Object>> jsonCars = cars.stream().map(car -> {

                Map<String, Object> map = new HashMap<>();
                map.put("type", car.type());
                map.put("model", car.model());
                map.put("brand", car.brand());
                map.put("prices", car.getPriceMap());
                map.put("releaseDate", car.releaseDate() != null ? car.releaseDate().toString() : null);

                return map;

            }).toList();

            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonCars);

        } catch (Exception e) {

            throw new RuntimeException("Failed to format JSON: " + e.getMessage());
        }
    }
}
