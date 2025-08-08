package com.yabets.carprocessor.model;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public LocalDate unmarshal(String v) {
        return LocalDate.parse(v, FORMATTER);
    }

    @Override
    public String marshal(LocalDate v) {
        return v.format(FORMATTER);
    }
}
