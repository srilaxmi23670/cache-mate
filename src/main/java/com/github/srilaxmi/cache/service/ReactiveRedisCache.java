package com.github.srilaxmi.cache.service;

import com.github.srilaxmi.cache.configuration.RedissonCustomClient;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import com.google.gson.Gson;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import com.google.common.reflect.TypeToken;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReactiveRedisCache {

    @Autowired
    private RedissonCustomClient redissonCustomClient;

    private static final Gson gson = new Gson();

    private RedissonClient redissonClient;

    private RedissonReactiveClient getReactiveRedisClient() {

        if (redissonClient != null) {
            return redissonClient
                    .reactive();
        }

        redissonClient = redissonCustomClient.getRedissonClient();

        return redissonClient.reactive();
    }

    public RLocalCachedMap<Object, Object> getLocalCachedMap(String name, LocalCachedMapOptions<Object, Object> options) {

        return redissonCustomClient.getRedissonClient().getLocalCachedMap(name, options);
    }

    private RMapReactive<String, String> getMap(String set) {
        return getReactiveRedisClient().getMap(set);
    }

    public <T> Mono<T> get(String set, String key, Class<T> tClass) {

        return getMap(set)
                .get(key)
                .map(value -> gson.fromJson(value, tClass));
    }

    public Mono<List<Document>> getToList(String set, String key) {

        return getMap(set)
                .get(key)
                .map(value -> gson.fromJson(value, new TypeToken<List<Document>>() {
                }.getType()));
    }

    public Mono<Map<String, List<Document>>> getToList(String set, Set<String> keys) {

        return getMap(set)
                .getAll(keys)
                .map(values -> values.keySet()
                        .stream()
                        .collect(Collectors.toMap(
                                key -> key,
                                key -> gson.fromJson(values.get(key), new TypeToken<List<Document>>() {
                                }.getType())
                        ))
                );
    }

    public Mono<Integer> getKeysCount(String set) {

        return getMap(set)
                .size();
    }

    public Mono<Boolean> put(
            String set,
            String key,
            Object value
    ) {

        return getMap(set).fastPut(key, gson.toJson(value));
    }

    public Mono<Boolean> put(
            String set,
            Map<String, Document> keyValueMap
    ) {

        Map<String, String> serialisedData = keyValueMap.keySet()
                .stream()
                .collect(Collectors.toMap(
                        key -> key,
                        key -> gson.toJson(keyValueMap.get(key))
                ));

        return getMap(set).putAll(serialisedData)
                .thenReturn(true);
    }

    public Mono<Boolean> putFromList(
            String set,
            Map<String, List<Document>> keyValueMap
    ) {

        Map<String, String> serialisedData = keyValueMap.keySet()
                .stream()
                .collect(Collectors.toMap(
                        key -> key,
                        key -> gson.toJson(keyValueMap.get(key))
                ));

        return getMap(set).putAll(serialisedData)
                .thenReturn(true);
    }

    public Mono<Boolean> delete(
            String set,
            String key
    ) {

        return getMap(set).fastRemove(key)
                .thenReturn(true);
    }

    public Mono<Boolean> delete(
            String set,
            Set<String> keys
    ) {

        return getMap(set).fastRemove(String.valueOf(keys))
                .thenReturn(true);
    }

    public Mono<Boolean> deleteSet(String set) {

        log.info("Deleting set :: {}", set);
        return getMap(set).delete();
    }

}
