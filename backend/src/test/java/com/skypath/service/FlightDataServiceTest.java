package com.skypath.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.skypath.model.Flight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlightDataServiceTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Instantiate the standard object mapper and explicitly add the JavaTimeModule 
        // to handle LocalDateTime parsing which is normally auto-configured by Spring Boot.
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private FlightDataService createServiceWithJson(String json) {
        Resource resource = new ByteArrayResource(json.getBytes(StandardCharsets.UTF_8));
        
        // Mock the ResourceLoader to always return our injected JSON string 
        // representing the test edge cases.
        ResourceLoader resourceLoader = new ResourceLoader() {
            @Override
            public Resource getResource(String location) {
                return resource;
            }

            @Override
            public ClassLoader getClassLoader() {
                return null;
            }
        };

        // We use a dummy dataPath. The custom ResourceLoader ignores it anyway.
        FlightDataService service = new FlightDataService(objectMapper, resourceLoader, "dummy.json");
        service.init();
        return service;
    }

    @Test
    void testValidFlightWithNumericPrice() {
        String json = """
            {
              "airports": [
                {"code": "JFK", "name": "John F. Kennedy International", "city": "New York", "country": "US", "timezone": "America/New_York"},
                {"code": "LAX", "name": "Los Angeles International", "city": "Los Angeles", "country": "US", "timezone": "America/Los_Angeles"}
              ],
              "flights": [
                {"flightNumber": "SP101", "airline": "SkyPath Airways", "origin": "JFK", "destination": "LAX", "departureTime": "2024-03-15T08:30:00", "arrivalTime": "2024-03-15T11:45:00", "price": 299.00, "aircraft": "A320"}
              ]
            }
            """;

        FlightDataService service = createServiceWithJson(json);
        
        List<Flight> flights = service.getFlights();
        assertEquals(1, flights.size(), "Should parse exactly 1 valid flight");
        
        Flight flight = flights.get(0);
        assertEquals("SP101", flight.flightNumber());
        
        // Use compareTo for BigDecimal equality regardless of trailing zeros
        assertEquals(0, new BigDecimal("299.00").compareTo(flight.price()));
    }

    @Test
    void testValidFlightWithStringPrice() {
        String json = """
            {
              "airports": [
                {"code": "JFK", "name": "John F. Kennedy", "city": "NY", "country": "US", "timezone": "America/New_York"},
                {"code": "LAX", "name": "LAX", "city": "LA", "country": "US", "timezone": "America/Los_Angeles"}
              ],
              "flights": [
                {"flightNumber": "SP102", "airline": "SkyPath", "origin": "JFK", "destination": "LAX", "departureTime": "2024-03-15T14:00:00", "arrivalTime": "2024-03-15T17:15:00", "price": "99", "aircraft": "B737"}
              ]
            }
            """;

        FlightDataService service = createServiceWithJson(json);
        
        List<Flight> flights = service.getFlights();
        assertEquals(1, flights.size(), "Should parse exactly 1 valid flight");
        
        Flight flight = flights.get(0);
        assertEquals("SP102", flight.flightNumber());
        assertEquals(0, new BigDecimal("99").compareTo(flight.price()));
    }

    @Test
    void testFlightWithInvalidOriginIsFilteredOut() {
        String json = """
            {
              "airports": [
                {"code": "LAX", "name": "LAX", "city": "LA", "country": "US", "timezone": "America/Los_Angeles"}
              ],
              "flights": [
                {"flightNumber": "SP103", "airline": "SkyPath", "origin": "JKF", "destination": "LAX", "departureTime": "2024-03-15T19:30:00", "arrivalTime": "2024-03-15T22:45:00", "price": 279.00, "aircraft": "A321"}
              ]
            }
            """;

        FlightDataService service = createServiceWithJson(json);
        
        List<Flight> flights = service.getFlights();
        
        // Since "JKF" is not mapped in the airports list (only LAX is), the flight should be skipped
        assertTrue(flights.isEmpty(), "Flight with invalid origin code should be filtered out");
    }
}
