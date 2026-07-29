package com.skypath.controller;

import com.skypath.dto.Itinerary;
import com.skypath.service.FlightSearchService;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/flights")
@Validated
@CrossOrigin(origins = "*")
public class FlightSearchController {

    private final FlightSearchService flightSearchService;

    public FlightSearchController(FlightSearchService flightSearchService) {
        this.flightSearchService = flightSearchService;
    }

    @GetMapping("/search")
    public List<Itinerary> searchFlights(
            @RequestParam @Pattern(regexp = "^[A-Z]{3}$", message = "Origin must be exactly 3 uppercase letters") String origin,
            @RequestParam @Pattern(regexp = "^[A-Z]{3}$", message = "Destination must be exactly 3 uppercase letters") String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return flightSearchService.searchFlights(origin, destination, date);
    }
}
