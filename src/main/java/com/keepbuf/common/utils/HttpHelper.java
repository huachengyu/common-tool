package com.keepbuf.common.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author huacy
 * @since 2018/05/09
 */
public class HttpHelper {

    private static final String UTF8 = "UTF-8";
    private static final String CONTENT_TYPE = "Content-Type";
    private final CloseableHttpClient httpClient;

    HttpHelper(int maxPerRoute, int maxTotal,
               String user, String password,
               int connectionTimeoutMillis, int socketTimeoutMillis) {

        // 池化管理
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(maxTotal);
        connManager.setDefaultMaxPerRoute(maxPerRoute);

        // 失败重试策略
        HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                // 如果已经重试了5次，就放弃
                if (executionCount >= 3) {
                    return false;
                }
                // 连接被拒绝
                if (exception instanceof ConnectTimeoutException) {
                    return true;
                }
                // 如果服务器丢掉了连接，那么就重试
                if (exception instanceof NoHttpResponseException) {
                    return true;
                }
                // socket time out
                if (exception instanceof SocketTimeoutException) {
                    return true;
                }
                // 连接被拒绝
                if (exception instanceof SocketException) {
                    return true;
                }
                // 不要重试SSL握手异常
                if (exception instanceof SSLHandshakeException) {
                    return false;
                }
                // 超时
                if (exception instanceof InterruptedIOException) {
                    return false;
                }
                // 目标服务器不可达
                if (exception instanceof UnknownHostException) {
                    return false;
                }
                // ssl握手异常
                if (exception instanceof SSLException) {
                    return false;
                }

                HttpClientContext clientContext = HttpClientContext.adapt(context);
                HttpRequest request = clientContext.getRequest();
                // 如果请求是幂等的，就再次尝试
                return !(request instanceof HttpEntityEnclosingRequest);
            }
        };

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectionTimeoutMillis)
                .setSocketTimeout(socketTimeoutMillis)
                .build();
        if (StringUtils.isNoneEmpty(user) && StringUtils.isNoneEmpty(password)) {
            CredentialsProvider provider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials
                    = new UsernamePasswordCredentials(user, password);
            provider.setCredentials(AuthScope.ANY, credentials);
            httpClient = HttpClients.custom()
                    .setConnectionManager(connManager)
                    .setDefaultCredentialsProvider(provider)
                    .setDefaultRequestConfig(requestConfig)
                    .setRetryHandler(httpRequestRetryHandler)
                    .build();
        } else {
            httpClient = HttpClients.custom()
                    .setConnectionManager(connManager)
                    .setDefaultRequestConfig(requestConfig)
                    .setRetryHandler(httpRequestRetryHandler)
                    .build();
        }
    }

    /**
     * head请求
     */
    public Result head(String url) {
        HttpHead httpHead = new HttpHead(url);
        return execute(httpHead);
    }

    /**
     * put请求
     */
    public Result put(String url, String jsonString) {
        HttpPut httpPut = new HttpPut(url);
        httpPut.setEntity(new StringEntity(jsonString, ContentType.APPLICATION_JSON));
        return execute(httpPut);
    }

    /**
     * post请求JSON
     */
    public Result post(String url, String jsonString) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new StringEntity(jsonString, ContentType.APPLICATION_JSON));
        return execute(httpPost);
    }

    /**
     * post请求表单
     */
    public Result post(String url, Map<String, String> form) {
        HttpPost httppost = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<>(form.size());
        for (Map.Entry<String, String> entry : form.entrySet()) {
            params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        httppost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
        return execute(httppost);
    }

    /**
     * get请求
     */
    public Result get(String url) {
        HttpGet httpGet = new HttpGet(url);
        return execute(httpGet);
    }

    private Result execute(HttpUriRequest request) {
        try {
            CloseableHttpResponse response = httpClient.execute(request);
            return processHttpResponse(response, UTF8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Result processHttpResponse(CloseableHttpResponse response, String encoding) {
        Header[] contentType = response.getHeaders(CONTENT_TYPE);
        String contentTypeEncoding = encoding;
        if (contentType != null && contentType.length > 0) {
            String[] arr = contentType[0].getValue().split("charset=");
            if (arr.length > 1) {
                contentTypeEncoding = arr[1];
            }
        }
        HttpEntity respEntity = response.getEntity();
        try {
            Result result = new Result();
            if (respEntity != null && respEntity.getContent() != null) {
                result.content = IOUtils.toString(respEntity.getContent(), getCharset(contentTypeEncoding));
            }
            result.headers = response.getAllHeaders();
            result.statusLine = response.getStatusLine();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            EntityUtils.consumeQuietly(respEntity);
            IOUtils.closeQuietly(response);
        }
    }

    private static Charset getCharset(String encoding) {
        Charset charset;
        try {
            charset = Charset.forName(encoding);
        } catch (Exception e) {
            charset = Charset.forName(UTF8);
        }
        return charset;
    }

    public class Result {
        private Header[] headers;
        private String content;
        private StatusLine statusLine;

        public Header[] getHeaders() {
            return headers;
        }

        public String getContent() {
            return content;
        }

        public int getStatusCode() {
            return statusLine.getStatusCode();
        }

        public String getReasonPhrase() {
            return statusLine.getReasonPhrase();
        }

        public boolean isSuccess() {
            return getStatusCode() == 200;
        }
    }
}
