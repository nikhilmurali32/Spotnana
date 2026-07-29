package com.skypath.service;

import com.skypath.dto.FlightSegment;
import com.skypath.dto.Itinerary;
import com.skypath.model.Airport;
import com.skypath.model.Flight;
import com.skypath.util.TimeCalculationUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

@Service
public class FlightSearchService {

    private final FlightDataService flightDataService;

    public FlightSearchService(FlightDataService flightDataService) {
        this.flightDataService = flightDataService;
    }

    private record PathState(List<Flight> flights, long totalTravelTime) {}

    public List<Itinerary> searchFlights(String origin, String destination, LocalDate date) {
        List<Itinerary> results = new ArrayList<>();
        PriorityQueue<PathState> pq = new PriorityQueue<>(Comparator.comparingLong(PathState::totalTravelTime));

        // Find all initial flights
        for (Flight f : flightDataService.getFlights()) {
            if (f.origin().equals(origin) && f.departureTime().toLocalDate().equals(date)) {
                Airport originAirport = flightDataService.getAirport(f.origin());
                Airport destAirport = flightDataService.getAirport(f.destination());
                
                ZonedDateTime dep = TimeCalculationUtils.parseToZonedDateTime(f.departureTime().toString(), originAirport.timezone());
                ZonedDateTime arr = TimeCalculationUtils.parseToZonedDateTime(f.arrivalTime().toString(), destAirport.timezone());
                
                long duration = TimeCalculationUtils.calculateDurationInMinutes(dep, arr);
                pq.add(new PathState(List.of(f), duration));
            }
        }

        while (!pq.isEmpty()) {
            PathState current = pq.poll();
            List<Flight> currentFlights = current.flights();
            Flight lastFlight = currentFlights.get(currentFlights.size() - 1);

            if (lastFlight.destination().equals(destination)) {
                results.add(convertToItinerary(currentFlights, current.totalTravelTime()));
                continue;
            }

            if (currentFlights.size() >= 3) {
                continue; // Cap traversal strictly at a maximum of 3 segments
            }

            Airport layoverAirport = flightDataService.getAirport(lastFlight.destination());
            ZonedDateTime arrivalAtLayover = TimeCalculationUtils.parseToZonedDateTime(
                    lastFlight.arrivalTime().toString(), layoverAirport.timezone());
            Airport airport1 = flightDataService.getAirport(lastFlight.origin());

            for (Flight nextFlight : flightDataService.getFlights()) {
                if (!nextFlight.origin().equals(lastFlight.destination())) {
                    continue; // Ensure passengers do not change airports during a layover
                }

                boolean alreadyVisited = currentFlights.stream()
                        .anyMatch(f -> f.origin().equals(nextFlight.destination()) || f.destination().equals(nextFlight.destination()));
                if (alreadyVisited) {
                    continue; // Cycle prevention
                }

                ZonedDateTime departureOfNext = TimeCalculationUtils.parseToZonedDateTime(
                        nextFlight.departureTime().toString(), layoverAirport.timezone());
                Airport airport2 = flightDataService.getAirport(nextFlight.destination());

                if (TimeCalculationUtils.isValidLayover(arrivalAtLayover, departureOfNext, airport1, airport2)) {
                    List<Flight> newFlights = new ArrayList<>(currentFlights);
                    newFlights.add(nextFlight);

                    ZonedDateTime startOfItinerary = TimeCalculationUtils.parseToZonedDateTime(
                            newFlights.get(0).departureTime().toString(), flightDataService.getAirport(newFlights.get(0).origin()).timezone());
                    Airport nextDestAirport = flightDataService.getAirport(nextFlight.destination());
                    ZonedDateTime endOfItinerary = TimeCalculationUtils.parseToZonedDateTime(
                            nextFlight.arrivalTime().toString(), nextDestAirport.timezone());

                    long newTotalTime = TimeCalculationUtils.calculateDurationInMinutes(startOfItinerary, endOfItinerary);
                    pq.add(new PathState(newFlights, newTotalTime));
                }
            }
        }

        // The results should already be sorted by total travel time due to PriorityQueue, 
        // but we explicitly sort to guarantee the contract.
        results.sort(Comparator.comparingLong(Itinerary::totalTravelTimeMinutes));
        return results;
    }

    private Itinerary convertToItinerary(List<Flight> flights, long totalTravelTime) {
        List<FlightSegment> segments = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (Flight f : flights) {
            Airport originAirport = flightDataService.getAirport(f.origin());
            Airport destAirport = flightDataService.getAirport(f.destination());
            
            ZonedDateTime dep = TimeCalculationUtils.parseToZonedDateTime(f.departureTime().toString(), originAirport.timezone());
            ZonedDateTime arr = TimeCalculationUtils.parseToZonedDateTime(f.arrivalTime().toString(), destAirport.timezone());
            
            long duration = TimeCalculationUtils.calculateDurationInMinutes(dep, arr);
            
            segments.add(new FlightSegment(
                    f.flightNumber(),
                    f.airline(),
                    f.origin(),
                    f.destination(),
                    dep,
                    arr,
                    duration
            ));
            
            if (f.price() != null) {
                totalPrice = totalPrice.add(f.price());
            }
        }

        return new Itinerary(segments, totalTravelTime, totalPrice);
    }
}
