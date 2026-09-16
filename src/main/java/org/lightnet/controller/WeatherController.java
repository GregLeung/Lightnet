package org.lightnet.controller;

import org.lightnet.service.WeatherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exposes the HTTP endpoint for weather lookups.
 */
@RestController
@RequestMapping("/v1/weather")
public class WeatherController {

    private static final Logger logger = LoggerFactory.getLogger(WeatherController.class);

    /**
     * Gets current weather for the requested location.
     */
    @GetMapping
    public WeatherResponse getWeather(
            @RequestParam("location") String location) {
        logger.debug("Received weather request for '{}'", location);
        return weatherService.getWeather(location);
    }

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }
}
