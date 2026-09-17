package org.lightnet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lightnet.controller.WeatherResponse;
import org.lightnet.exceptions.LocationBlankException;
import org.lightnet.providers.OpenWeatherMapProvider;
import org.lightnet.exceptions.WeatherProviderException;
import org.lightnet.providers.WeatherStackProvider;
import org.lightnet.service.WeatherService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private WeatherStackProvider primaryProvider;

    @Mock
    private OpenWeatherMapProvider fallbackProvider;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache staleCache;

    @org.junit.jupiter.api.BeforeEach
    void configureCache() {
        lenient().when(cacheManager.getCache("weather-stale")).thenReturn(staleCache);
    }

    @InjectMocks
    private WeatherService weatherService;

    @Test
    void returnsWeatherFromPrimaryProvider() {
        var expected = new WeatherResponse(12.5, 20.0);
        when(primaryProvider.getWeather("Singapore,SG")).thenReturn(expected);

        var actual = weatherService.getWeather("Singapore,SG");

        assertEquals(expected, actual);
        verify(primaryProvider).getWeather("Singapore,SG");
        verify(fallbackProvider, never()).getWeather("Singapore,SG");
    }

    @Test
    void usesFallbackProviderWhenPrimaryProviderFails() {
        var expected = new WeatherResponse(8.0, 25.0);
        when(primaryProvider.getWeather("Singapore,SG"))
                .thenThrow(new WeatherProviderException("primary failure"));
        when(fallbackProvider.getWeather("Singapore,SG")).thenReturn(expected);

        var actual = weatherService.getWeather("Singapore,SG");

        assertEquals(expected, actual);
        verify(primaryProvider).getWeather("Singapore,SG");
        verify(fallbackProvider).getWeather("Singapore,SG");
    }

    @Test
    void throwsCombinedExceptionWhenBothProvidersFail() {
        var primaryFailure = new WeatherProviderException("primary failure");
        var fallbackFailure = new WeatherProviderException("fallback failure");
        when(primaryProvider.getWeather("Singapore,SG")).thenThrow(primaryFailure);
        when(fallbackProvider.getWeather("Singapore,SG")).thenThrow(fallbackFailure);

        var exception = assertThrows(
                WeatherProviderException.class,
                () -> weatherService.getWeather("Singapore,SG"));

        assertEquals("All weather providers failed", exception.getMessage());
        assertEquals(fallbackFailure, exception.getCause());
        assertEquals(1, fallbackFailure.getSuppressed().length);
        assertEquals(primaryFailure, fallbackFailure.getSuppressed()[0]);
    }

    @Test
    void servesStaleWeatherWhenBothProvidersFail() {
        var staleResponse = new WeatherResponse(7.0, 22.0);
        when(primaryProvider.getWeather("Singapore,SG"))
                .thenThrow(new WeatherProviderException("primary failure"));
        when(fallbackProvider.getWeather("Singapore,SG"))
                .thenThrow(new WeatherProviderException("fallback failure"));
        when(staleCache.get("singapore,sg", WeatherResponse.class)).thenReturn(staleResponse);

        var actual = weatherService.getWeather("Singapore,SG");

        assertEquals(staleResponse, actual);
        verify(staleCache).get("singapore,sg", WeatherResponse.class);
    }

    @Test
    void rejectsBlankLocation() {
        var exception = assertThrows(
                LocationBlankException.class,
                () -> weatherService.getWeather("   "));

        assertEquals("location must not be blank", exception.getMessage());
        verify(primaryProvider, never()).getWeather("   ");
        verify(fallbackProvider, never()).getWeather("   ");
    }
}
