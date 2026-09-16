package org.lightnet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.lightnet.controller.WeatherResponse;
import org.lightnet.providers.OpenWeatherMapProvider;
import org.lightnet.providers.WeatherStackProvider;
import org.lightnet.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
