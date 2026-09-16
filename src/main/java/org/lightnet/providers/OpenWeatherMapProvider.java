package org.lightnet.providers;

import org.lightnet.controller.WeatherResponse;
import org.lightnet.exceptions.WeatherProviderException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fallback weather provider backed by the OpenWeatherMap current-weather API.
 */
@Service
public class OpenWeatherMapProvider implements WeatherProvider {

    private static final Logger logger = LoggerFactory.getLogger(OpenWeatherMapProvider.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;

    public OpenWeatherMapProvider(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${weather.open-weather-map.base-url:https://api.openweathermap.org}") String baseUrl,
            @Value("${weather.open-weather-map.api-key:}") String apiKey) {
        this.restTemplate = restTemplateBuilder.build();
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    /**
     * Fetches current weather and converts a successful provider response to
     * the application response model.
     */
    @Override
    public WeatherResponse getWeather(String location) {
        if (apiKey.isBlank()) {
            logger.debug("OpenWeatherMap request skipped because the API key is not configured");
            throw new WeatherProviderException("OpenWeatherMap API key is not configured");
        }

        logger.debug("Requesting weather from OpenWeatherMap for '{}'", location);
        var uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/data/2.5/weather")
                .queryParam("q", location.trim())
                .queryParam("appid", apiKey)
                .build()
                .toUri();

        try {
            var response = restTemplate.getForObject(uri, OpenWeatherMapResponse.class);
            if (response == null || response.cod() == null || response.cod() != 200) {
                logger.debug("OpenWeatherMap returned an unsuccessful response for '{}'", location);
                throw new WeatherProviderException("OpenWeatherMap returned an error");
            }
            logger.debug("OpenWeatherMap returned weather for '{}'", location);
            return toWeatherResponse(response);
        } catch (RestClientException exception) {
            logger.debug("OpenWeatherMap request failed for '{}'", location, exception);
            throw new WeatherProviderException("OpenWeatherMap request failed", exception);
        }
    }

    private WeatherResponse toWeatherResponse(OpenWeatherMapResponse response) {
        return new WeatherResponse(
                response.wind() == null ? null : response.wind().speed(),
                response.main() == null ? null : response.main().temp());
    }
}
