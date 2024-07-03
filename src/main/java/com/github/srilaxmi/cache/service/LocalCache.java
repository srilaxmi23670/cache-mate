package com.github.srilaxmi.cache.service;

import com.github.srilaxmi.cache.constants.EntityCacheName;
import com.github.srilaxmi.cache.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLocalCachedMap;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class LocalCache implements Cache {

    /**
     * A RLocalCachedMap is a special map data structure provided by Redis.
     * It is basically just a local in-memory hashMap (with one special property) that contains a copy of the central
     * cached data. Each java instance will have and maintain its own local hashMap. When a hit comes in, the local
     * hashMap is checked first, and then if the data is not found, central Redis cache is checked.
     *
     * The special property of these RLocalCachedMap instances is that they seamlessly sync themselves with the central
     * redis cache whenever data has to be fetched/cached, while still providing the simple interface of a regular
     * hashMap.
     *
     * All actions pertaining to keeping RLocalCachedMap in sync with Central Cache are handled under the hood. The
     * sync logic can be customized by providing relevant options at the time the RLocalCachedMap instances are created.
     */
    private RLocalCachedMap<Object, Object> map;

    // The name of the cache.
    private final EntityCacheName entityCacheName;

    // For performance tracking
    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();

    public LocalCache(EntityCacheName entityCacheName, RLocalCachedMap<Object, Object> map) {
        this.entityCacheName = entityCacheName;
        this.map = map;
    }

    @Override
    @NonNull
    public String getName() {
        return entityCacheName.name();
    }

    @Override
    @NonNull
    public RLocalCachedMap<Object, Object> getNativeCache() {
        return map;
    }

    @Override
    public ValueWrapper get(@NonNull Object key) {
        try {
            Object value = map.get(key);
            if (value == null) {
                log.info("Entry for key {} not found in {} cache", key, entityCacheName.name());
                addCacheMiss();
                return null;
            }
            addCacheHit();
            Object cachedObject = JsonUtil.fromJsonString((String) value, entityCacheName.getClazz(), entityCacheName.getTypeReference());
            if (cachedObject == null) {
                log.error("Failed to deserialize object :: {} :: {}", value, key);
                return null;
            }
//        log.info("Returning cached value for entry with key {} from {} cache", key, entityCacheName.name());
            return new SimpleValueWrapper(cachedObject);
        } catch (Exception e) {
            log.error("Error in LocalCache get", e);
            return null;
        }
    }

    @Override
    public <T> T get(@NonNull Object key, Class<T> type) {
        // This method doesn't seem to be used by Spring cache
        // We will throw an error just in-case, so that we know if this method gets erroneously called.
        log.error("Unexpected get(Object key, Class<T> type) method of RedisCache called!");
        return null;
    }

    @Override
    public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
        // This method doesn't seem to be used by Spring cache
        // We will throw an error just in-case, so that we know if this method gets erroneously called.
        log.error("Unexpected get(Object key, Callable<T> valueLoader) method of RedisCache called!");
        return null;
    }

    @Override
    public void put(@NonNull Object key, Object value) {
//        log.info("Inserting entry for key:{} and value:{} into {} cache", key, JsonUtil.toJsonString(value), entityCacheName.name());
        try {
            map.fastPut(key, JsonUtil.toJsonString(value));
        } catch (Exception e) {
            log.error("Error in LocalCache put", e);
        }
    }

    @Override
    public void evict(@NonNull Object key) {
        try {
            map.fastRemove(key);
        } catch (Exception e) {
            log.error("Error in LocalCache evict", e);
        }
    }

    /**
     * Evict a list of keys from cache
     */
    public void evict(@NonNull List<Object> keys) {
        try {
            log.info("Removing entries for keys:{} from {} cache", JsonUtil.toJsonString(keys), entityCacheName.name());
            map.fastRemove(keys.toArray(new Object[0]));
        } catch (Exception e) {
            log.error("Error in LocalCache evict", e);
        }
    }

    /**
     * Evict all keys from cache
     */
    @Override
    public void clear() {
        try {
            log.info("Clearing all entries for {} cache", entityCacheName.name());
            map.clear();
        } catch (Exception e) {
            log.error("Error in LocalCache clear", e);
        }
    }

    /**
     * Clears ONLY the local in-memory CacheMap of all java instances.
     *
     * This can be useful when you have manually manipulated the data in central cache and want to force the all your
     * java instances to dump their local caches and re-read the new data from central cache.
     */
    public void clearLocalDataOnly() {
        log.info("Clearing all entries for {} cache from local cacheMap only", entityCacheName.name());
        map.clearLocalCache();
    }


    /**
     * The number of get requests that were satisfied by the cache.
     * @return the number of hits
     */
    public long getCacheHits(){
        return hits.get();
    }

    /**
     * A miss is a get request that is not satisfied.
     * @return the number of misses
     */
    public long getCacheMisses(){
        return misses.get();
    }

    /**
     * Increment the hits counter
     */
    private void addCacheHit(){
        hits.incrementAndGet();
    }

    /**
     * Increment the misses counter
     */
    private void addCacheMiss(){
        misses.incrementAndGet();
    }

    public long getCacheSize() {
        return map.size();
    }

    public long getCacheSizeInMemory() {
        return map.sizeInMemory();
    }

    public Set<Object> getCachedKeySet() {
        return map.cachedKeySet();
    }
}
