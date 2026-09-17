package org.lightnet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.lightnet.controller.WeatherResponse;
import org.lightnet.exceptions.WeatherProviderException;
import org.lightnet.providers.OpenWeatherMapProvider;
import org.lightnet.providers.WeatherStackProvider;
import org.lightnet.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class WeatherServiceCacheTest {

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private WeatherStackProvider primaryProvider;

    @MockBean
    private OpenWeatherMapProvider fallbackProvider;

    @AfterEach
    void clearCache() {
        var cache = cacheManager.getCache("weather");
        if (cache != null) {
            cache.clear();
        }
        var staleCache = cacheManager.getCache("weather-stale");
        if (staleCache != null) {
            staleCache.clear();
        }
    }

    @Test
    void getWeatherCachesResultForSameLocation() {
        var expected = new WeatherResponse(12.5, 20.0);
        when(primaryProvider.getWeather("Singapore,SG")).thenReturn(expected);

        var first = weatherService.getWeather("Singapore,SG");
        var second = weatherService.getWeather("Singapore,SG");

        assertEquals(expected, first);
        assertEquals(expected, second);
        verify(primaryProvider, times(1)).getWeather("Singapore,SG");
    }

    @Test
    void servesStaleResultWhenFreshCacheIsGoneAndProvidersFail() {
        var expected = new WeatherResponse(12.5, 20.0);
        when(primaryProvider.getWeather("Singapore,SG")).thenReturn(expected);

        assertEquals(expected, weatherService.getWeather("Singapore,SG"));
        cacheManager.getCache("weather").clear();
        reset(primaryProvider, fallbackProvider);
        when(primaryProvider.getWeather("Singapore,SG"))
                .thenThrow(new WeatherProviderException("primary failure"));
        when(fallbackProvider.getWeather("Singapore,SG"))
                .thenThrow(new WeatherProviderException("fallback failure"));

        assertEquals(expected, weatherService.getWeather("Singapore,SG"));
        verify(primaryProvider).getWeather("Singapore,SG");
        verify(fallbackProvider).getWeather("Singapore,SG");
    }
}
