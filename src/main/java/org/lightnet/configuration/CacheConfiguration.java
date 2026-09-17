package org.lightnet.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/**
 * Configures the Caffeine cache used for weather responses.
 */
@Configuration
public class CacheConfiguration {

    private final Duration cacheDuration;
    private final Duration staleCacheDuration;

    public CacheConfiguration(
            @Value("${weather.cache.duration:3s}") Duration cacheDuration,
            @Value("${weather.cache.stale-duration:24h}") Duration staleCacheDuration) {
        this.cacheDuration = cacheDuration;
        this.staleCacheDuration = staleCacheDuration;
    }

    /**
     * Creates separate fresh and stale caches so expired responses remain
     * available when both providers are unavailable.
     */
    @Bean
    public CacheManager cacheManager() {
        var freshCache = new CaffeineCache(
                "weather",
                Caffeine.newBuilder()
                        .expireAfterWrite(cacheDuration)
                        .maximumSize(1_000)
                        .build());
        var staleCache = new CaffeineCache(
                "weather-stale",
                Caffeine.newBuilder()
                        .expireAfterWrite(staleCacheDuration)
                        .maximumSize(1_000)
                        .build());

        var cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(freshCache, staleCache));
        cacheManager.initializeCaches();
        return cacheManager;
    }
}
