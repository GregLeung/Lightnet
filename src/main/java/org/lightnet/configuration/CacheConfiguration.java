package org.lightnet.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configures the Caffeine cache used for weather responses.
 */
@Configuration
public class CacheConfiguration {

    private final Duration cacheDuration;

    public CacheConfiguration(@Value("${weather.cache.duration:3s}") Duration cacheDuration) {
        this.cacheDuration = cacheDuration;
    }

    /**
     * Creates the weather cache with a configurable expiry duration and size
     * limit.
     */
    @Bean
    public CacheManager cacheManager() {
        var cacheManager = new CaffeineCacheManager("weather");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(cacheDuration)
                .maximumSize(1_000));
        return cacheManager;
    }
}
