package com.github.srilaxmi.cache.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponseMeta {

    private Long count;
    private String source;
    private Boolean hasMoreData;
}

