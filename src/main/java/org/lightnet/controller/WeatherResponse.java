package org.lightnet.controller;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WeatherResponse(
        @JsonProperty("wind_speed") Double windSpeed,
        @JsonProperty("temperature_degrees") Double temperatureDegrees) {
}
