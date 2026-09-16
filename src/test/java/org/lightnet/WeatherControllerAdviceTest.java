package org.lightnet;

import org.junit.jupiter.api.Test;
import org.lightnet.advice.WeatherControllerAdvice;
import org.lightnet.controller.WeatherController;
import org.lightnet.exceptions.LocationBlankException;
import org.lightnet.exceptions.WeatherProviderException;
import org.lightnet.service.WeatherService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WeatherControllerAdviceTest {

    private final WeatherService weatherService = mock(WeatherService.class);
    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new WeatherController(weatherService))
            .setControllerAdvice(new WeatherControllerAdvice())
            .build();

    @Test
    void returnsInternalServerErrorWhenWeatherProvidersAreUnavailable() throws Exception {
        when(weatherService.getWeather("Singapore,SG"))
                .thenThrow(new WeatherProviderException("provider failure"));

        mvc.perform(get("/weather").param("location", "Singapore,SG"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.code", is(500)))
                .andExpect(jsonPath("$.error", is("All Weather Provider are unavailable.")));
    }

    @Test
    void returnsUnauthorizedWhenLocationIsMissing() throws Exception {
        mvc.perform(get("/weather"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.code", is(401)))
                .andExpect(jsonPath("$.error", is("Location is required.")));
    }

    @Test
    void returnsUnauthorizedWhenLocationIsBlank() throws Exception {
        when(weatherService.getWeather("   "))
                .thenThrow(new LocationBlankException("location must not be blank"));

        mvc.perform(get("/weather").param("location", "   "))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.code", is(401)))
                .andExpect(jsonPath("$.error", is("Location is required.")));
    }
}
