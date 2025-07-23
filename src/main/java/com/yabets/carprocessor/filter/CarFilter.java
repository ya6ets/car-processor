package com.yabets.carprocessor.filter;

import com.yabets.carprocessor.model.Car;

import java.util.List;

public interface CarFilter {
    List<Car> filter(List<Car> cars);
}