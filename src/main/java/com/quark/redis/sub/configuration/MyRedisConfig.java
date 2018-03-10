package com.quark.redis.sub.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * Created by ZhenpengLu on 2018/3/9.
 */
@Configuration
@ImportResource(locations = {"classpath:spring-redis.xml"})
public class MyRedisConfig {
}
