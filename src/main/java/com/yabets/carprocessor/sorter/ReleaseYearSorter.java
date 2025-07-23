package com.yabets.carprocessor.sorter;

import com.yabets.carprocessor.model.Car;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ReleaseYearSorter implements CarSorter {

    @Override
    public List<Car> sort(List<Car> cars) {

        return cars.stream()
                .sorted(Comparator.comparing(car -> car.releaseDate() != null ? car.releaseDate() : LocalDate.MIN, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }
}