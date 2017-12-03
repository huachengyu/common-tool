package com.keepbuf.common.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 *
 * @author huacy
 * @since 2017/12/03
 */
public class LocalCache {

    private static Cache<String, Object> cache = CacheBuilder
            .newBuilder()
            .maximumSize(Long.MAX_VALUE)
            // 指定项在一定时间内没有创建/覆盖时，会移除该key
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();

    public static void putCache(String key, Object value) {
        cache.put(key, value);
    }

    public static Object getCache(String key) {
        return cache.getIfPresent(key);
    }
}
