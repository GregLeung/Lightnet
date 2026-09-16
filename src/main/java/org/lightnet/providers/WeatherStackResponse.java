package org.lightnet.providers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherStackResponse(
        Request request,
        Location location,
        Current current) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Request(
            String type,
            String query,
            String language,
            String unit) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Location(
            String name,
            String country,
            String region,
            String lat,
            String lon,
            @JsonProperty("timezone_id") String timezoneId,
            String localtime,
            @JsonProperty("localtime_epoch") Long localtimeEpoch,
            @JsonProperty("utc_offset") String utcOffset) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Current(
            @JsonProperty("observation_time") String observationTime,
            Double temperature,
            @JsonProperty("weather_code") Integer weatherCode,
            @JsonProperty("weather_icons") List<String> weatherIcons,
            @JsonProperty("weather_descriptions") List<String> weatherDescriptions,
            Astro astro,
            @JsonProperty("air_quality") AirQuality airQuality,
            @JsonProperty("wind_speed") Double windSpeed,
            @JsonProperty("wind_degree") Integer windDegree,
            @JsonProperty("wind_dir") String windDirection,
            Integer pressure,
            Double precip,
            Integer humidity,
            Integer cloudcover,
            Integer feelslike,
            @JsonProperty("uv_index") Integer uvIndex,
            Integer visibility,
            @JsonProperty("is_day") String isDay) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Astro(
            String sunrise,
            String sunset,
            String moonrise,
            String moonset,
            @JsonProperty("moon_phase") String moonPhase,
            @JsonProperty("moon_illumination") Integer moonIllumination) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AirQuality(
            String co,
            String no2,
            String o3,
            String so2,
            @JsonProperty("pm2_5") String pm25,
            String pm10,
            @JsonProperty("us-epa-index") String usEpaIndex,
            @JsonProperty("gb-defra-index") String gbDefraIndex) {
    }
}
