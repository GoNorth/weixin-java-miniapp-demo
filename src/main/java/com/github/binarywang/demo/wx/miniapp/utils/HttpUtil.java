package com.github.binarywang.demo.wx.miniapp.utils;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Http工具
 */
public class HttpUtil {
    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    /**
     * Http连接超时时间
     */
    private static final int CONNECT_TIMEOUT = 1000 * 600;
    /**
     * Http 写入超时时间
     */
    private static final int WRITE_TIMEOUT = 1000 * 600;
    /**
     * Http Read超时时间
     */
    private static final int READ_TIMEOUT = 1000 * 600;
    /**
     * Http Async Call Timeout
     */
    private static final int CALL_TIMEOUT = 1000 * 600;
    /**
     * Http连接池
     */
    private static final int CONNECTION_POOL_SIZE = 1000;

    /**
     * 静态连接池对象
     */
    private static final ConnectionPool CONNECTION_POOL = new ConnectionPool(CONNECTION_POOL_SIZE, 30, TimeUnit.MINUTES);

    private static final OkHttpClient HTTP_CLIENT;

    static {
        HTTP_CLIENT = getHttpClient();
    }

    /**
     * 获取Http Client对象
     */
    public static OkHttpClient getHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .callTimeout(CALL_TIMEOUT, TimeUnit.MILLISECONDS)
                .connectionPool(CONNECTION_POOL)
                .build();
    }

    /**
     * http get
     *
     * @param url url
     * @return 响应内容字节数组
     */
    public static byte[] getBytes(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = HTTP_CLIENT.newCall(request).execute();
            return Objects.requireNonNull(response.body()).bytes();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("http get failed!");
        }
    }

    /**
     * http get with cookie
     *
     * @param url url
     * @param cookieStr cookie字符串
     * @return 响应内容字节数组
     */
    public static byte[] getBytesWithCookie(String url, String cookieStr) {
        Request request = new Request.Builder()
                .url(url)
                .header("Cookie", cookieStr)
                .build();
        try {
            Response response = HTTP_CLIENT.newCall(request).execute();
            return Objects.requireNonNull(response.body()).bytes();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("http get failed!");
        }
    }

    /**
     * http post
     *
     * @param url  url
     * @param body 请求body字符串
     * @return 响应内容字节数组
     */
    public static byte[] postBytes(String url, String body) {
        RequestBody requestBody = RequestBody.create(body, MediaType.parse("application/json;charset=utf-8"));
        Request request = new Request.Builder()
                .post(requestBody)
                .url(url)
                .build();
        try {
            Response response = HTTP_CLIENT.newCall(request).execute();
            return Objects.requireNonNull(response.body()).bytes();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("http post failed!");
        }
    }

    /**
     * http post
     *
     * @param url  url
     * @param body 请求body字符串
     * @return 响应内容字节数组
     */
    public static byte[] postBytes2(String url, String body, String cookieStr) {
        RequestBody requestBody = RequestBody.create(body, MediaType.parse("application/json;charset=utf-8"));
        Request request = new Request.Builder()
                .post(requestBody)
                .url(url)
                .header("Cookie", cookieStr)
                .build();
        try {
            Response response = HTTP_CLIENT.newCall(request).execute();
            return Objects.requireNonNull(response.body()).bytes();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("http post failed!");
        }
    }

    /**
     * http post with custom headers
     * 参考：D:/code2/jimeng-mcp-main-250912/src/api.ts 中的 request 方法
     *
     * @param url  url
     * @param body 请求body字符串
     * @param headers 自定义请求头Map
     * @return 响应内容字节数组
     */
    public static byte[] postBytesWithHeaders(String url, String body, Map<String, String> headers) {
        RequestBody requestBody = RequestBody.create(body, MediaType.parse("application/json;charset=utf-8"));
        Request.Builder requestBuilder = new Request.Builder()
                .post(requestBody)
                .url(url);
        
        //添加所有请求头
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.header(entry.getKey(), entry.getValue());
            }
        }
        
        Request request = requestBuilder.build();
        try {
            Response response = HTTP_CLIENT.newCall(request).execute();
            return Objects.requireNonNull(response.body()).bytes();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("http post failed!");
        }
    }

    /**
     * http post with form params
     * 使用 application/x-www-form-urlencoded 格式发送表单数据
     *
     * @param url  url
     * @param formParams 表单参数Map
     * @return 响应内容字节数组
     */
    public static byte[] postFormParams(String url, Map<String, String> formParams) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        
        // 添加所有表单参数
        if (formParams != null) {
            for (Map.Entry<String, String> entry : formParams.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    formBuilder.add(entry.getKey(), entry.getValue());
                }
            }
        }
        
        RequestBody requestBody = formBuilder.build();
        Request request = new Request.Builder()
                .post(requestBody)
                .url(url)
                .build();
        
        try {
            Response response = HTTP_CLIENT.newCall(request).execute();
            return Objects.requireNonNull(response.body()).bytes();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("http post form params failed!");
        }
    }
}

