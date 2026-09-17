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
    stale-duration: 24h
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
    stale-duration: 24h
```

Spring duration values such as `500ms`, `30s`, `5m`, and `1h` are supported.
When both weather providers are unavailable, the service serves the most
recent successful response while it remains in the stale cache. The stale
cache duration defaults to 24 hours and is independently configurable.

## Implementation

Stale Results are served from the cache when both providers fail. The cache 
is implemented with 24 hours expiration by default. We could have used a 
even longer expiration time, but this could make the cache to grow too large.
That's why trade-offs need to be considered when configuring cache settings.

The full responses of both weather providers are returned, this is to allow
future enhancements to the service, such as returning more weather information.

## Future Enhancements (if there is more time)

Use distributed caching to allow multiple instances of the service to share the same
cache. This would allow the service to scale horizontally and handle more
requests. It would also allow the service to be deployed in a cloud environment such
as K8s with multiple instances.

Add more logs for a better observability of the service. This would allow us to 
monitor the service such as usage(when there is a request coming in), 
performance (the duration to serve the request) and errors (how many 4XX, 5XX in certain
duration).

Build time level integration tests to ensure we know if there is a breaking change 
in the provider API. (i.e. using github actions to run the integration tests on a 
schedule, such as every day or every week and also every build).

More different kind of errors handling, such as when the provider API is down, or when
the provider API returns an error response. This would allow us to handle different 
kind of errors and return appropriate responses to the client.