package com.yabets.carprocessor.output;

import com.yabets.carprocessor.model.Car;

import java.util.List;

public interface OutputFormatter {

    String format(List<Car> cars);
}