package com.skypath.dto;

import java.math.BigDecimal;
import java.util.List;

public record Itinerary(
    List<FlightSegment> segments,
    long totalTravelTimeMinutes,
    BigDecimal totalPrice
) {}
