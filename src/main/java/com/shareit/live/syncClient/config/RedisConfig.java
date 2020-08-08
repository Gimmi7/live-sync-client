package com.shareit.live.syncClient.config;

import com.shareit.live.framework.redis.lettuce.LettuceClusterUtil;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: wangcy
 * @DateTime: 2020/4/24 20:13
 */
@Configuration
public class RedisConfig {
    @Value("${spring.profiles.active}")
    private String active;

    @Bean
    public RedisAdvancedClusterCommands<String, String> clusterCommands() {
        return LettuceClusterUtil.clusterCommands(this.getRedisUri());
    }

    private String getRedisUri() {
        switch (active) {
            case "test": {
                return "redis://test.main.ugc.sg2.redis:6379";
            }
            default: {
                return "redis://dev.api.ugc.sg2.redis:6379";
            }
        }
    }
}
