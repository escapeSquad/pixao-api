package com.escape.pixao.global.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
@EnableRedisRepositories
class RedisConfiguration(
    @Value("\${spring.data.redis.host}")
    val redisHost: String,
    @Value("\${spring.data.redis.port}")
    val redisPort: Int,
) {
    @Bean
    fun lettuceConnectionFactory(): LettuceConnectionFactory {
        val redisStandaloneConfiguration = RedisStandaloneConfiguration(redisHost, redisPort)

        val lettuceClientConfiguration = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ZERO)
            .shutdownTimeout(Duration.ZERO)
            .build()

        return LettuceConnectionFactory(redisStandaloneConfiguration, lettuceClientConfiguration)
    }

    @Bean
    fun redisTemplate(): RedisTemplate<*, *> {
        return RedisTemplate<Any, Any>().apply {
            this.connectionFactory = lettuceConnectionFactory()
            this.keySerializer = StringRedisSerializer()
            this.hashKeySerializer = StringRedisSerializer()
            this.valueSerializer = StringRedisSerializer()
        }
    }

    @Bean
    fun cacheManager(): CacheManager {
        val configuration = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    StringRedisSerializer()
                )
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    GenericJackson2JsonRedisSerializer()
                )
            )
            .entryTtl(
                Duration.ofMinutes(2)
            )
            .disableCachingNullValues()

        return RedisCacheManager.RedisCacheManagerBuilder
            .fromConnectionFactory(lettuceConnectionFactory())
            .cacheDefaults(configuration)
            .build()
    }
}