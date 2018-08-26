package com.keepbuf.common.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by IntelliJ IDEA.
 *
 * @author huacy
 * @since 2018/05/09
 */
public class HttpHelperFactory {

    private static final ConcurrentMap<String, HttpHelper> HTTP_HELPER_POOLS = new ConcurrentHashMap<>();

    public static class HttpHelperBuilder {
        private String user;
        private String password;
        private int maxPerRoute = 100;
        private int maxTotal = 100;
        private int connectionTimeout = 3000;
        private int socketTimeout = 3000;

        public HttpHelperBuilder setUser(String user) {
            this.user = user;
            return this;
        }

        public HttpHelperBuilder setPassword(String password) {
            this.password = password;
            return this;
        }

        public HttpHelperBuilder setMaxPerRoute(int maxPerRoute) {
            this.maxPerRoute = maxPerRoute;
            return this;
        }

        public HttpHelperBuilder setMaxTotal(int maxTotal) {
            this.maxTotal = maxTotal;
            return this;
        }

        public HttpHelperBuilder setConnectionTimeout(int connectionTimeoutMillis) {
            this.connectionTimeout = connectionTimeoutMillis;
            return this;
        }

        public HttpHelperBuilder setSocketTimeout(int socketTimeoutMillis) {
            this.socketTimeout = socketTimeoutMillis;
            return this;
        }

        public HttpHelper getHttpHelper(String key) {
            return HttpHelperFactory.getHttpHelper(key, this);

        }
    }

    public static HttpHelperBuilder custom() {
        return new HttpHelperBuilder();
    }


    private static HttpHelper getHttpHelper(String key, HttpHelperBuilder builder) {
        HttpHelper httpHelper = null;
        try {
            if (!HTTP_HELPER_POOLS.containsKey(key)) {
                httpHelper = new HttpHelper(builder.maxPerRoute,
                        builder.maxTotal,
                        builder.user,
                        builder.password,
                        builder.connectionTimeout,
                        builder.socketTimeout);
                HTTP_HELPER_POOLS.putIfAbsent(key, httpHelper);
            } else {
                httpHelper = HTTP_HELPER_POOLS.get(key);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
        return httpHelper;
    }
}
