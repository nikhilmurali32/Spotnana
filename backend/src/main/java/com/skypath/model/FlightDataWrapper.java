package com.skypath.model;

import java.util.List;

public record FlightDataWrapper(
    List<Airport> airports,
    List<Flight> flights
) {}
