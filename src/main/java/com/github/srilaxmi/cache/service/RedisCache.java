package com.github.srilaxmi.cache.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.srilaxmi.cache.configuration.RedissonCustomClient;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.cache.CacheException;
import java.io.IOException;

@Service
@Slf4j
public class RedisCache {

    @Autowired
    private RedissonCustomClient redissonCustomClient;

    private static final ObjectMapper mapper = new ObjectMapper();

    private static RedissonClient redissonClient;

    private RedissonClient getRedisClient() {

        if (redissonClient != null) {
            return redissonClient;
        }

        redissonClient = redissonCustomClient.getRedissonClient();

        return redissonClient;
    }

    public RLocalCachedMap<Object, Object> getLocalCachedMap(String name, LocalCachedMapOptions<Object, Object> options) {
        return redissonCustomClient.getRedissonClient().getLocalCachedMap(name, options);
    }

    public RMapCache<Object, Object> getRMapCache(String name, LocalCachedMapOptions<Object, Object> options) {
        return redissonCustomClient.getRedissonClient().getMapCache(name);
    }

    public Object get(String set, String key) {

        RedissonClient redisClient = getRedisClient();

        RMapCache<String, Object> map = redisClient.getMapCache(set);

        try {
            if (map.containsKey(key)) {

                String value = (String) map.get(key);
                return mapper.readValue(value, Object.class);

            }
        } catch (IOException e) {
            log.error("Exception occurred while getting data from redis : ", e);
            throw new CacheException(e.getMessage(), e);
        } finally {
            map.destroy();
        }

        return null;

    }

    public long getKeysCount(String set) {

        RedissonClient redisClient = getRedisClient();

        RMapCache<String, Object> map = redisClient.getMapCache(set);

        return map.keySet().size();
    }

    public void put(
            String set,
            String key,
            Object value
    ) {

        RedissonClient redisClient = getRedisClient();
        RMapCache<String, Object> map = redisClient.getMapCache(set);
        Gson gson = new Gson();
        map.fastPut(key, gson.toJson(value));

        map.destroy();

    }

}