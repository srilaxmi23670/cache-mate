package com.github.srilaxmi.cache.controller;

import com.github.srilaxmi.cache.service.ReactiveRedisCache;
import com.github.srilaxmi.cache.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/cache-mate")
public class CacheController {

    @Autowired
    private ReactiveRedisCache reactiveRedisCache;

    @GetMapping("/{setName}/data/{keyName}")
    public Mono<ApiResponse<Object>> getDataFromRedis(
            @PathVariable String setName,
            @PathVariable String keyName
    ) {

        return ApiResponse.ok(reactiveRedisCache.get(setName, keyName, Object.class));
    }

    @GetMapping("/{setName}/list-data/{keyName}")
    public Mono<ApiResponse<List<Document>>> getMultiDataFromRedis(
            @PathVariable String setName,
            @PathVariable String keyName
    ) {

        return ApiResponse.ok(reactiveRedisCache.getToList(setName, keyName));
    }

    @GetMapping("/{setName}/count")
    public Mono<ApiResponse<Integer>> getDataCountFromRedis(
            @PathVariable String setName
    ) {

        return ApiResponse.ok(reactiveRedisCache.getKeysCount(setName));
    }

    @PostMapping("/{setName}/data/{keyName}")
    public Mono<ApiResponse<Boolean>> pushDataToRedis(
            @PathVariable String setName,
            @PathVariable String keyName,
            @RequestBody Document doc
    ) {

        return ApiResponse.ok(reactiveRedisCache.put(setName, keyName, doc));
    }

    @PostMapping("/{setName}/list-data/{keyName}")
    public Mono<ApiResponse<Boolean>> pushListDataToRedis(
            @PathVariable String setName,
            @PathVariable String keyName,
            @RequestBody List<Document> doc
    ) {

        return ApiResponse.ok(reactiveRedisCache.putFromList(setName, Map.of(keyName, doc)));

    }

}
