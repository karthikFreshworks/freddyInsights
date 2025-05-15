package com.freshworks.freddy.insights.config;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.params.SetParams;

@Service
@AllArgsConstructor
public class RedisClient {
    private final JedisPool jedisPool;

    public String getValue(String key) {
        try (var jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }

    public void setValue(String key, String value) {
        try (var jedis = jedisPool.getResource()) {
            SetParams setParams = new SetParams();
            jedis.set(key, value, setParams);
        }
    }

    public boolean exists(String key) {
        try (var jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        }
    }

    public Pipeline getPipeline() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pipelined();
        }
    }

    public String scriptLoad(String luaScript) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.scriptLoad(luaScript);
        }
    }
}
