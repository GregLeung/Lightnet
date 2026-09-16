# Introduction

Lightnet is a Spring Boot weather service that retrieves current weather for a
location. It uses WeatherStack as the primary provider and automatically falls
back to OpenWeatherMap when the primary provider is unavailable. Successful
responses are cached with Caffeine to reduce repeated calls to external
weather APIs.

## Features

- `GET /v1/weather?location={location}` weather lookup endpoint
- WeatherStack primary provider
- OpenWeatherMap fallback provider
- Configurable weather-response cache duration
- OpenAPI specification in `src/main/resources/swagger.yaml`
- Swagger UI provided by Springdoc
- Centralized validation and provider error responses
- Debug logging for the critical request and provider paths

## Requirements

- Java JDK 21
- Maven

The project includes Maven Wrapper scripts, so Maven does not need to be
installed globally.

## Configuration

Configuration is stored in `src/main/resources/application.yaml`. API keys can
be supplied through environment variables without changing the source code:

```yaml
weather:
  cache:
    duration: 3s
  weather-stack:
    base-url: https://api.weatherstack.com
    access-key: ${WEATHERSTACK_ACCESS_KEY:}
  open-weather-map:
    base-url: https://api.openweathermap.org
    api-key: ${OPENWEATHERMAP_API_KEY:}
```

Set the provider credentials before running the application. For example, in a Unix shell:

```sh
export WEATHERSTACK_ACCESS_KEY="your-weatherstack-access-key"
export OPENWEATHERMAP_API_KEY="your-openweathermap-api-key"
```

or just put your API key here like this:
```yaml
weather:
  cache:
    duration: 3s
  weather-stack:
    base-url: https://api.weatherstack.com
    access-key: ${WEATHERSTACK_ACCESS_KEY:YOUR_WEATHERSTACK_API_KEY}
  open-weather-map:
    base-url: https://api.openweathermap.org
    api-key: ${OPENWEATHERMAP_API_KEY:YOUR_OPEN_WEATHER_MAP_API_KEY}
```

The `${ENVIRONMENT_VARIABLE:default}` syntax lets Spring use an environment
variable when it is available and a safe configured default otherwise. Keeping
all provider URLs, credentials, and cache settings configurable makes the
application easier to deploy across local, test, staging, and production
environments without rebuilding the application or editing Java code. It also
allows provider endpoints and cache policy to be changed independently of the
business logic.

## Build

Build and run the test suite with the Maven Wrapper:

```sh
./mvnw clean test
```

Create the packaged application:

```sh
./mvnw clean package
```

## Run

Run directly with Spring Boot:

```sh
./mvnw spring-boot:run
```

The application starts on the default Spring Boot port, `8080`.

## API

Request:

```text
GET http://localhost:8080/v1/weather?location=Singapore
```

Successful response:

```json
{
  "wind_speed": 4.1,
  "temperature_degrees": 30.5
}
```

If the location is missing or blank, the service returns HTTP `401`. If both
weather providers fail, it returns HTTP `500`.

The OpenAPI definition is available at
`src/main/resources/swagger.yaml`. When the application is running, Springdoc
also exposes the generated API documentation at:

```text
http://localhost:8080/swagger-ui.html
```

## Caching

Weather responses are cached by normalized location. The default cache
expiration is three seconds and can be changed in `application.yaml`, for
example:

```yaml
weather:
  cache:
    duration: 30s
```

Spring duration values such as `500ms`, `30s`, `5m`, and `1h` are supported.
