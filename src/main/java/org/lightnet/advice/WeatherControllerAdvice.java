package org.lightnet.advice;

import org.lightnet.exceptions.LocationBlankException;
import org.lightnet.exceptions.WeatherProviderException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts provider and location-validation failures into API error responses.
 */
@RestControllerAdvice
public class WeatherControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(WeatherControllerAdvice.class);

    @ExceptionHandler(WeatherProviderException.class)
    public ResponseEntity<ErrorResponse> handleWeatherProviderException(
            WeatherProviderException exception) {
        logger.debug("Returning 500 because weather providers are unavailable", exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "All Weather Provider are unavailable."));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestParameter(
            MissingServletRequestParameterException exception) {
        logger.debug("Returning 401 because the location parameter is missing");
        return unauthorizedLocationResponse();
    }

    @ExceptionHandler(LocationBlankException.class)
    public ResponseEntity<ErrorResponse> handleBlankLocation(
            LocationBlankException exception) {
        logger.debug("Returning 401 because the location is blank");
        return unauthorizedLocationResponse();
    }

    private ResponseEntity<ErrorResponse> unauthorizedLocationResponse() {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(401, "Location is required."));
    }

    public record ErrorResponse(int code, String error) {
    }
}
