package com.yabets.carprocessor.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class Price {

    @XmlAttribute
    private String currency;
    @XmlValue
    private Double value;

    // Required for JAXB
    public Price() {
    }

    public Price(String currency, Double value) {
        this.currency = currency;
        this.value = value;
    }

    public String currency() { return currency; }
    public Double value() { return value; }
}
