package com.github.srilaxmi.cache.service;


import com.github.srilaxmi.cache.constants.EntityCacheName;
import com.github.srilaxmi.cache.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RLocalCachedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Responsible for the instantiation and management of RedisCache instances.
 *
 * One redisCache instance corresponds to one entity.
 * For example, TurtlemintMasterCache handles caching of TurtlemintMaster.
 */
@Component
@Slf4j
public class LocalCacheManager implements CacheManager {

    private static final Integer DEFAULT_CACHE_SIZE = 100;

    @Autowired
    private ReactiveRedisCache reactiveRedisCache;

    // A map to store the existing cache instances
    private static final HashMap<String, LocalCache> caches = new HashMap<>();

    /**
     * Gets the custom config options for the cache by name. These config options
     * are fetched using the cacheConfiguration module. For now, cacheSize can be customized.
     */
    public LocalCachedMapOptions<Object, Object> getOptions(Integer cacheSize) {

        return LocalCachedMapOptions.defaults()
                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.LRU)
                .syncStrategy(LocalCachedMapOptions.SyncStrategy.UPDATE)
                .cacheSize(cacheSize);
    }

    /**
     * Used to fetch a RedisCache instance. This RedisCache is either present in memory or built if it doesn't
     * exist already. This method helps to conceal the buildIfNotPresent behavior.
     */
    @NonNull
    public LocalCache getCache(@NonNull String name) {
        LocalCache cache;
        if (caches.containsKey(name))
            cache = caches.get(name);
        else {
            LocalCachedMapOptions<Object, Object> options = getOptions(DEFAULT_CACHE_SIZE);
            log.info("Cache for {} does not exist. Creating new Cache with config: {}",
                     name, JsonUtil.toJsonString(options));

            RLocalCachedMap<Object, Object> redisCacheMap = reactiveRedisCache.getLocalCachedMap(name, options);
            cache = new LocalCache(EntityCacheName.valueOf(name), redisCacheMap);
            caches.put(name, cache);
        }
        return cache;
    }

    /**
     * Returns the list of names of caches that currently exist
     */
    @NonNull
    public List<String> getCacheNames() {
        return new ArrayList<>(caches.keySet());
    }

    /**
     * Delete a specific local cache instance.
     *
     * This method would be useful when you have changed the cache configs and want to reinitialize the local cache
     * instances to use the new configs, while still leaving the central Cached data intact.
     *
     * Calling this method should only delete the local cache on all java instances. At the next 'get'/'put' call
     * the local cache will get recreated with the new configs. If it is a 'get' call, it will even check central cache
     * for the required data before hitting DB.
     *
     * TODO - As of now this method does not work as it should.
     * Current Behavior: This method will only delete the local cache instance from the current java instance only,
     * Expected Behavior: This method should delete the local cache instance from all of the java instances.
     *
     * Note on Clear vs Delete:
     *      Clear local cache = Clearing all entries from the local cache instance
     *      Delete local cache = Delete the local cache instance itself
     *                          (forcing it to be recreated with the new configs)
     *
     * Currently, even though this method will clear the local cache across all instances, it will not delete them and
     * force them to be recreated with new configs across all instances. This is because the clearing of the cacheMap
     * is handled under the hood by redis and synced across all instances, whereas the removal/deletion of the cacheMap
     * entirely from 'caches' is an action on a regular HashMap, and so this would not be synced across all instances.
     *
     * Will need additional research into how this can be achieved. However, this is of very low priority as it
     * has rarely been used.
     */
    public void deleteLocalCache(@NonNull String name) {
        LocalCache cache = getCache("");
        log.info("Deleting Local CacheMap {}", name);
        cache.clearLocalDataOnly();
        caches.remove(name);
    }

}
