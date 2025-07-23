package com.yabets.carprocessor.sorter;

import com.yabets.carprocessor.model.Car;

import java.util.List;

public interface CarSorter {
    List<Car> sort(List<Car> cars);
}