package com.keepbuf.common.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author huacy
 * @since 2017/12/03
 */
public class HttpPool {

    private static Logger log = Logger.getLogger(HttpPool.class);

    private static final String CHAR_SET = "UTF-8";

    /**
     * 最大连接数400
     */
    private static final int MAX_CONNECTION_NUM = 400;

    /**
     * 单路由最大连接数80
     */
    private static final int MAX_PER_ROUTE = 80;

    /**
     * 向服务端请求超时时间设置(单位:毫秒)
     */
    private static final int SERVER_REQUEST_TIME_OUT = 2000;

    /**
     * 服务端响应超时时间设置(单位:毫秒)
     */
    private static final int SERVER_RESPONSE_TIME_OUT = 3000;
    /**
     * 连接池管理对象
     */
    private static PoolingHttpClientConnectionManager cm = null;

    static {
        // 初始化HTTP/HTTPS 连接池
        SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
        try {
            sslContextBuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
                    sslContextBuilder.build());
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("https", socketFactory)
                    .register("http", new PlainConnectionSocketFactory())
                    .build();
            cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            cm.setMaxTotal(MAX_CONNECTION_NUM);
            cm.setDefaultMaxPerRoute(MAX_PER_ROUTE);
        } catch (Exception e) {
            log.error("@init PoolingHttpClientConnectionManager Error", e);
        }
    }


    /**
     * 创建线程安全的HttpClient
     *
     * @param config 客户端超时设置
     */
    private static CloseableHttpClient getHttpsClient(RequestConfig config) {
        return HttpClients.custom().setDefaultRequestConfig(config)
                .setConnectionManager(cm)
                .build();
    }


    /**
     * Https post请求
     *
     * @param url  请求地址
     * @param json 请求参数(如果为null,则表示不请求参数) return 返回结果
     */
    public static String httpPost(String url, JSONObject json) {
        CloseableHttpResponse response = null;
        HttpPost post = null;
        try {
            // connectTimeout设置服务器请求超时时间
            // socketTimeout设置服务器响应超时时间
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(SERVER_REQUEST_TIME_OUT)
                    .setConnectTimeout(SERVER_RESPONSE_TIME_OUT).build();
            post = new HttpPost(url);
            post.setConfig(requestConfig);
            if (json != null) {
                StringEntity se = new StringEntity(json.toString(), CHAR_SET);
                se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;"));
                post.setEntity(se);
            }
            response = getHttpsClient(requestConfig).execute(post);
            int status = response.getStatusLine().getStatusCode();
            String result = null;
            if (status == 200) {
                result = EntityUtils.toString(response.getEntity(), CHAR_SET);
            }
            EntityUtils.consume(response.getEntity());
            response.close();
            return result;
        } catch (Exception e) {
            if (e instanceof SocketTimeoutException) {
                // 服务器请求超时
                log.error("@httpPost SocketTimeoutException ： ", e);
            } else if (e instanceof ConnectTimeoutException) {
                // 服务器响应超时(已经请求了)
                log.error("@httpPost ConnectTimeoutException : ", e);
            }
            log.error("@httpPost Exception : ", e);
        } finally {
            if (null != post) {
                post.releaseConnection();
            }
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                    response.close();
                } catch (IOException e) {
                    log.error("@httpPost IOException ： ", e);
                }
            }
        }
        return null;
    }

    public static String httpPut(String url, JSONObject json) {
        CloseableHttpResponse response = null;
        HttpPut put = null;
        try {
            // connectTimeout设置服务器请求超时时间
            // socketTimeout设置服务器响应超时时间
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(SERVER_REQUEST_TIME_OUT)
                    .setConnectTimeout(SERVER_RESPONSE_TIME_OUT).build();
            put = new HttpPut(url);
            put.setConfig(requestConfig);
            if (json != null) {
                StringEntity se = new StringEntity(json.toString(), CHAR_SET);
                se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;"));
                put.setEntity(se);
            }
            response = getHttpsClient(requestConfig).execute(put);
            int status = response.getStatusLine().getStatusCode();
            String result = null;
            if (status == 200) {
                result = EntityUtils.toString(response.getEntity(), CHAR_SET);
            }
            EntityUtils.consume(response.getEntity());
            response.close();
            return result;
        } catch (Exception e) {
            if (e instanceof SocketTimeoutException) {
                // 服务器请求超时
                log.error("@httpPut SocketTimeoutException ： ", e);
            } else if (e instanceof ConnectTimeoutException) {
                // 服务器响应超时(已经请求了)
                log.error("@httpPut ConnectTimeoutException : ", e);
            }
            log.error("@httpPut Exception : ", e);
        } finally {
            if (null != put) {
                put.releaseConnection();
            }
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                    response.close();
                } catch (IOException e) {
                    log.error("@httpPut IOException ： ", e);
                }
            }
        }
        return null;
    }

    /**
     * Get方式取得URL的内容.
     *
     * @param url 访问的网址
     */
    public static String httpGet(String url, Map<String, Object> params) {
        url = url + "?" + urlEncode(params);
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            // connectTimeout设置服务器请求超时时间
            // socketTimeout设置服务器响应超时时间
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(SERVER_REQUEST_TIME_OUT)
                    .setConnectTimeout(SERVER_RESPONSE_TIME_OUT).build();
            httpGet.setConfig(requestConfig);
            // 执行请求
            response = getHttpsClient(requestConfig).execute(httpGet, HttpClientContext.create());
            // 转换结果
            HttpEntity entity1 = response.getEntity();
            String json = EntityUtils.toString(entity1);
            // 消费掉内容
            EntityUtils.consume(entity1);
            return json;
        } catch (IOException e) {
            log.error("@httpGet IOException : ", e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    log.error("@httpGet IOException : ", e);
                }
            }
            httpGet.releaseConnection();
        }
        return null;
    }

    /**
     * 将map型转为请求参数型
     */
    private static String urlEncode(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry i : data.entrySet()) {
            try {
                sb.append(i.getKey()).append("=").append(URLEncoder.encode(i.getValue() + "", "UTF-8")).append("&");
            } catch (UnsupportedEncodingException e) {
                log.error("@urlEncode UnsupportedEncodingException", e);
            }
        }
        return sb.toString();
    }
}
