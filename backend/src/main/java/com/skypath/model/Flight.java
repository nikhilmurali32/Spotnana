package com.skypath.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Flight(
    String flightNumber,
    String airline,
    String origin,
    String destination,
    LocalDateTime departureTime,
    LocalDateTime arrivalTime,
    @JsonDeserialize(using = PriceDeserializer.class)
    BigDecimal price,
    String aircraft
) {
    public static class PriceDeserializer extends JsonDeserializer<BigDecimal> {
        @Override
        public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getValueAsString();
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            try {
                // Strip out non-numeric characters except decimals in case of dirty currency formatting
                value = value.replaceAll("[^\\d.]", "");
                if (value.isEmpty()) return null;
                return new BigDecimal(value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
}
