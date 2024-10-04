package com.example.SeniorProject.Configuration;

import org.springframework.context.annotation.*;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.jedis.*;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.*;

@Configuration
public class RedisConfiguration
{
    @Bean
    public JedisConnectionFactory redisConnectionFactory()
    {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setHostName("test-6ehgbr.serverless.usw1.cache.amazonaws.com");
        factory.setPort(6379);
        factory.setTimeout(20000); // Set timeout to 20 seconds
        return factory;
    }
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory)
    {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
}
