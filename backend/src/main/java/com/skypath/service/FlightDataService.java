package com.skypath.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skypath.model.Airport;
import com.skypath.model.Flight;
import com.skypath.model.FlightDataWrapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FlightDataService {

    private static final Logger log = LoggerFactory.getLogger(FlightDataService.class);

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final String dataPath;

    private Map<String, Airport> airports;
    private List<Flight> flights;

    public FlightDataService(
            ObjectMapper objectMapper, 
            ResourceLoader resourceLoader,
            @Value("${FLIGHTS_DATA_PATH:classpath:flights.json}") String dataPath) {
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.dataPath = dataPath;
    }

    @PostConstruct
    public void init() {
        try {
            // Ensure proper Resource resolution for absolute file paths without prefixes
            String resolvedPath = dataPath;
            if (!resolvedPath.startsWith("classpath:") && !resolvedPath.startsWith("file:") && !resolvedPath.startsWith("http")) {
                resolvedPath = "file:" + resolvedPath;
            }

            Resource resource = resourceLoader.getResource(resolvedPath);
            try (InputStream is = resource.getInputStream()) {
                // Parse the JSON directly into our strongly typed wrapper record
                FlightDataWrapper data = objectMapper.readValue(is, FlightDataWrapper.class);
                
                // Store airports in a Map for O(1) lookup
                this.airports = data.airports() != null 
                    ? data.airports().stream().collect(Collectors.toMap(Airport::code, Function.identity()))
                    : Map.of();
                    
                // Validate and store flights
                List<Flight> parsedFlights = data.flights() != null ? data.flights() : List.of();
                List<Flight> validFlights = new ArrayList<>();
                for (Flight flight : parsedFlights) {
                    if (flight.origin() == null || flight.destination() == null) {
                        log.warn("Flight {} missing origin or destination, skipping.", flight.flightNumber());
                        continue;
                    }
                    if (!this.airports.containsKey(flight.origin())) {
                        log.warn("Flight {} has unknown origin code '{}', skipping.", flight.flightNumber(), flight.origin());
                        continue;
                    }
                    if (!this.airports.containsKey(flight.destination())) {
                        log.warn("Flight {} has unknown destination code '{}', skipping.", flight.flightNumber(), flight.destination());
                        continue;
                    }
                    validFlights.add(flight);
                }
                this.flights = List.copyOf(validFlights);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load flight data from " + dataPath, e);
        }
    }

    public Airport getAirport(String code) {
        return airports.get(code);
    }

    public Map<String, Airport> getAirports() {
        return airports;
    }

    public List<Flight> getFlights() {
        return flights;
    }
}
