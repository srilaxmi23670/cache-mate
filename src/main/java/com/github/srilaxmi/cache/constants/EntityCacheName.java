package com.github.srilaxmi.cache.constants;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.swing.text.Document;

@Getter
@AllArgsConstructor
public enum EntityCacheName {

    TEST_CACHE(Document .class, null);

    private final Class clazz;
    private final TypeReference typeReference;
}
