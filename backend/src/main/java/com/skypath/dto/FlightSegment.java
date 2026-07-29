package com.skypath.dto;

import java.time.ZonedDateTime;

public record FlightSegment(
    String flightNumber,
    String airlineCode,
    String departureAirportCode,
    String arrivalAirportCode,
    ZonedDateTime departureTime,
    ZonedDateTime arrivalTime,
    long durationMinutes
) {}
