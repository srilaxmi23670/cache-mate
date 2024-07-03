package com.github.srilaxmi.cache.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    private String message;
    private T data;
    private ApiResponseMeta meta;

    public static <T> Mono<ApiResponse<List<T>>> ok(Flux<T> dataFlux) {

        return dataFlux
                .collectList()
                .map(data -> {

                    return ApiResponse
                            .<List<T>>builder()
                            .data(data)
                            .build();

                });
    }

    public static <T> Mono<ApiResponse<T>> ok(Mono<T> dataMono) {

        return dataMono
                .map(data -> ApiResponse
                        .<T>builder()
                        .data(data)
                        .build()
                );
    }

    public static <T> ApiResponse<T> ok(T data) {

        return ApiResponse
                .<T>builder()
                .data(data)
                .build();
    }

    public static <T> ApiResponse<List<T>> ok(List<T> data) {

        return ApiResponse
                .<List<T>>builder()
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(Exception e) {

        return error(e.getMessage());
    }

    public static <T> ApiResponse<T> error(String message) {

        return ApiResponse
                .<T>builder()
                .message(message)
                .build();
    }

}