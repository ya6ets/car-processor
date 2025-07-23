package com.yabets.carprocessor.output;

import com.yabets.carprocessor.model.Car;
import com.yabets.carprocessor.model.Cars;
import com.yabets.carprocessor.model.Price;
import com.yabets.carprocessor.model.Prices;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

import java.io.StringWriter;
import java.util.List;

public class XmlFormatter implements OutputFormatter {

    @Override
    public String format(List<Car> cars) {

        try {

            Cars xmlCars = new Cars();
            xmlCars.setCars(cars.stream().map(car -> {

                Prices prices = new Prices();

                List<Price> priceList = car.getPriceMap().entrySet().stream()
                        .map(entry -> new Price(entry.getKey(), entry.getValue()))
                        .toList();

                prices.setPrices(priceList);

                return new Car(car.type(), car.model(), prices, car.brand(), car.releaseDate());

            }).toList());

            JAXBContext context = JAXBContext.newInstance(Cars.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter writer = new StringWriter();
            marshaller.marshal(xmlCars, writer);

            return writer.toString();

        } catch (JAXBException e) {

            throw new RuntimeException("Failed to format XML: " + e.getMessage());
        }
    }
}
