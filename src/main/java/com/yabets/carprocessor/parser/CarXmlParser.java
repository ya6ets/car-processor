package com.yabets.carprocessor.parser;

import com.yabets.carprocessor.model.Car;
import com.yabets.carprocessor.model.Price;
import com.yabets.carprocessor.model.Prices;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CarXmlParser {
    private static final Logger LOGGER = Logger.getLogger(CarXmlParser.class.getName());

    // Temporary class to handle the standalone <price currency="USD"> element
    @XmlRootElement(name = "car")
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class RawCar {

        @XmlElement
        private String type;
        @XmlElement
        private String model;
        @XmlElement(name = "price")
        private Price usdPrice;
        @XmlElement
        private Prices prices;

        public Car toCar() {

            Prices combinedPrices = new Prices();
            List<Price> priceList = prices != null && prices.getPrices() != null ? prices.getPrices() : new ArrayList<>();

            if (usdPrice != null && usdPrice.currency() != null && usdPrice.value() != null) {

                priceList.add(0, usdPrice); // Add USD price first
            }

            combinedPrices.setPrices(priceList);

            return new Car(type, model, combinedPrices, null, null);
        }
    }

    @XmlRootElement(name = "cars")
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class RawCars {

        @XmlElement(name = "car")
        private List<RawCar> cars;
    }

    public List<Car> parse(InputStream xmlStream) throws JAXBException {

        try {

            JAXBContext context = JAXBContext.newInstance(RawCars.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            RawCars rawCars = (RawCars) unmarshaller.unmarshal(xmlStream);

            return rawCars.cars.stream()
                    .map(RawCar::toCar)
                    .collect(Collectors.toList());

        } catch (JAXBException e) {

            LOGGER.severe("JAXB parsing error: " + e.getMessage());

            if (e.getLinkedException() != null) {
                LOGGER.severe("Details: " + e.getLinkedException().getMessage());
            }

            throw e;
        }
    }
}
