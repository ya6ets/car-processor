package com.yabets.carprocessor.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

@XmlRootElement(name = "car")
@XmlAccessorType(XmlAccessType.FIELD)
public class Car {

    @XmlElement
    private String type;
    @XmlElement
    private String model;
    @XmlElement
    private Prices prices;
    private String brand;
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate releaseDate;

    // Required for JAXB
    public Car() {
    }

    public Car(String type, String model, Prices prices, String brand, LocalDate releaseDate) {

        this.type = type;
        this.model = model;
        this.prices = prices;
        this.brand = brand;
        this.releaseDate = releaseDate;
    }

    // Getters
    public String type() { return type; }
    public String model() { return model; }
    public Prices prices() { return prices; }
    public String brand() { return brand; }
    public LocalDate releaseDate() { return releaseDate; }

    // Normalize currency keys to uppercase
    public Map<String, Double> getPriceMap() {

        if (prices == null || prices.getPrices() == null) {

            return Map.of();
        }

        return prices.getPrices().stream()
                .filter(price -> price.currency() != null && price.value() != null)
                .collect(Collectors.toMap(
                        price -> price.currency().toUpperCase(),
                        Price::value,
                        (v1, v2) -> v1
                ));
    }
}
