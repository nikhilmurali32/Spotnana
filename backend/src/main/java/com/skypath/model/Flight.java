package com.skypath.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Flight(
    String flightNumber,
    String airline,
    String origin,
    String destination,
    LocalDateTime departureTime,
    LocalDateTime arrivalTime,
    BigDecimal price,
    String aircraft
) {}
