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

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FlightDataService {

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
                    
                // Store flights in a list
                this.flights = data.flights() != null 
                    ? data.flights() 
                    : List.of();
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
