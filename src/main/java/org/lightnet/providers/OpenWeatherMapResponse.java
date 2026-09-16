package org.lightnet.providers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenWeatherMapResponse(
        Coordinates coord,
        List<Weather> weather,
        String base,
        Main main,
        Integer visibility,
        Wind wind,
        Clouds clouds,
        Long dt,
        Sys sys,
        Integer timezone,
        Long id,
        String name,
        Integer cod) {

    public record Coordinates(
            Double lon,
            Double lat) {
    }

    public record Weather(
            Integer id,
            String main,
            String description,
            String icon) {
    }

    public record Main(
            Double temp,
            Double feels_like,
            Double temp_min,
            Double temp_max,
            Integer pressure,
            Integer humidity,
            Integer sea_level,
            Integer grnd_level) {
    }

    public record Wind(
            Double speed,
            Integer deg) {
    }

    public record Clouds(
            Integer all) {
    }

    public record Sys(
            Integer type,
            Long id,
            String country,
            Long sunrise,
            Long sunset) {
    }
}
