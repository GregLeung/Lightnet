package org.lightnet.service;

import org.lightnet.controller.WeatherResponse;
import org.lightnet.exceptions.LocationBlankException;
import org.lightnet.exceptions.WeatherProviderException;
import org.lightnet.providers.WeatherStackProvider;
import org.lightnet.providers.OpenWeatherMapProvider;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Coordinates weather lookup, validation, caching, and provider fallback.
 */
@Service
public class WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);

    private final WeatherStackProvider primaryProvider;
    private final OpenWeatherMapProvider fallbackProvider;
    private final CacheManager cacheManager;

    public WeatherService(
            WeatherStackProvider primaryProvider,
            OpenWeatherMapProvider fallbackProvider,
            CacheManager cacheManager) {
        this.primaryProvider = primaryProvider;
        this.fallbackProvider = fallbackProvider;
        this.cacheManager = cacheManager;
    }

    /**
     * Returns cached weather when available, otherwise tries the primary
     * provider before delegating to the fallback provider.
     */
    @Cacheable(
            cacheNames = "weather",
            key = "#location == null ? '' : #location.trim().toLowerCase()")
    public WeatherResponse getWeather(String location) {
        logger.debug("Fetching weather for location '{}'", location);
        validateLocation(location);

        try {
            logger.debug("Calling primary weather provider for '{}'", location);
            return cacheSuccessfulResponse(location, primaryProvider.getWeather(location));
        } catch (WeatherProviderException primaryFailure) {
            logger.debug("Primary weather provider failed for '{}'; using fallback provider",
                    location, primaryFailure);
            try {
                logger.debug("Calling fallback weather provider for '{}'", location);
                return cacheSuccessfulResponse(location, fallbackProvider.getWeather(location));
            } catch (WeatherProviderException fallbackFailure) {
                logger.debug("Fallback weather provider also failed for '{}'", location,
                        fallbackFailure);
                fallbackFailure.addSuppressed(primaryFailure);
                var staleResponse = staleCache().get(
                        normalizeLocation(location),
                        WeatherResponse.class);
                if (staleResponse != null) {
                    logger.warn("Both weather providers failed for '{}'; serving stale cached data",
                            location);
                    return staleResponse;
                }
                throw new WeatherProviderException(
                        "All weather providers failed",
                        fallbackFailure);
            }
        }
    }

    private WeatherResponse cacheSuccessfulResponse(String location, WeatherResponse response) {
        staleCache().put(normalizeLocation(location), response);
        return response;
    }

    private Cache staleCache() {
        var cache = cacheManager.getCache("weather-stale");
        if (cache == null) {
            throw new IllegalStateException("Weather stale cache is not configured");
        }
        return cache;
    }

    private String normalizeLocation(String location) {
        return location == null ? "" : location.trim().toLowerCase();
    }

    private void validateLocation(String location) {
        if (location == null || location.isBlank()) {
            logger.debug("Rejected weather request because location is blank");
            throw new LocationBlankException("location must not be blank");
        }
    }
}
