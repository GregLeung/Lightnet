package org.lightnet.providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
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
 * Primary weather provider backed by the WeatherStack current-weather API.
 *
 * <p>Provider and response failures are translated to
 * {@link WeatherProviderException} so the service can invoke its fallback
 * provider consistently.</p>
 */
@Service
public class WeatherStackProvider implements WeatherProvider {

    private static final Logger logger = LoggerFactory.getLogger(WeatherStackProvider.class);

    private final RestTemplate restTemplate;
    private final JsonMapper jsonMapper;
    private final String baseUrl;
    private final String accessKey;

    public WeatherStackProvider(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${weather.weather-stack.base-url}") String baseUrl,
            @Value("${weather.weather-stack.access-key}") String accessKey) {
        this.restTemplate = restTemplateBuilder.build();
        this.jsonMapper = JsonMapper.builder().build();
        this.baseUrl = baseUrl;
        this.accessKey = accessKey;
    }

    /**
     * Fetches current weather and maps the provider response to the application
     * response model.
     */
    @Override
    public WeatherResponse getWeather(String location) {
        if (accessKey.isBlank()) {
            logger.debug("WeatherStack request skipped because the access key is not configured");
            throw new WeatherProviderException("WeatherStack access key is not configured");
        }

        logger.debug("Requesting weather from WeatherStack for '{}'", location);
        var uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/current")
                .queryParam("access_key", accessKey)
                .queryParam("query", location)
                .build()
                .toUri();



        try {
            var responseBody = restTemplate.getForObject(uri, String.class);
            if (responseBody == null || responseBody.isBlank()) {
                logger.debug("WeatherStack returned an empty response for '{}'", location);
                throw new WeatherProviderException("WeatherStack returned an empty response");
            }
            if (responseBody.contains("\"error\"")) {
                logger.debug("WeatherStack returned an error for '{}'", location);
                throw new WeatherProviderException("WeatherStack returned an error");
            }

            var response = jsonMapper.readValue(responseBody, WeatherStackResponse.class);
            if (response.current() == null) {
                logger.debug("WeatherStack response did not contain current weather for '{}'",
                        location);
                throw new WeatherProviderException("WeatherStack response does not contain current weather");
            }

            logger.debug("WeatherStack returned weather for '{}'", location);
            return new WeatherResponse(
                    response.current().windSpeed() == null ? null : response.current().windSpeed(),
                    response.current().temperature() == null ? null : response.current().temperature());
        } catch (RestClientException | JsonProcessingException exception) {
            logger.debug("WeatherStack request failed for '{}'", location, exception);
            throw new WeatherProviderException("WeatherStack request failed", exception);
        }
    }
}
