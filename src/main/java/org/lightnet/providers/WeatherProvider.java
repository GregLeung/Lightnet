package org.lightnet.providers;

import org.lightnet.controller.WeatherResponse;

public interface WeatherProvider {

    WeatherResponse getWeather(String location);
}
