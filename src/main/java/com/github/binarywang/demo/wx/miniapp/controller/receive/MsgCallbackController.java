package com.github.binarywang.demo.wx.miniapp.controller.receive;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import com.github.binarywang.demo.wx.miniapp.utils.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 通用回调接收接口（用于第三方 webhook / 回调推送）。
 *
 * <p>路径：/msg（无 api 前缀），与 SalesUploadController 同级包。</p>
 */
@RestController
public class MsgCallbackController {

    private static final Logger logger = LoggerFactory.getLogger(MsgCallbackController.class);
    private static final String WX_ID_WEATHER = "cherfei0611";
    private static final String WEATHER_COMMAND = "#指令-天气";
    private static final String IMAGE_COMMAND = "#指令-图片";
    private static final long DEDUP_TTL_MS = 5_000L;
    private static final ConcurrentHashMap<String, Long> DEDUP_REQ_CONTENT = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> DEDUP_RESP_MSG = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> DEDUP_OUTBOUND_REQ = new ConcurrentHashMap<>();
    private static final AtomicLong LAST_CLEANUP_MS = new AtomicLong(0L);

    /**
     * 兼容部分平台的回调“连通性校验/握手”。
     * 常见参数：echostr / challenge
     */
    @GetMapping("/msg")
    public String verify(@RequestParam(required = false) Map<String, String> params,
                         @RequestHeader(required = false) Map<String, String> headers,
                         HttpServletRequest request) {
        Map<String, String> safeParams = (params == null) ? Collections.emptyMap() : params;
        Map<String, String> safeHeaders = sanitizeHeaders(headers);
        logger.info("GET /msg callback verify. remote={}, params={}, headers={}",
                request.getRemoteAddr(),
                JSON.toJSONString(safeParams),
                JSON.toJSONString(safeHeaders));

        // 常见握手字段
        String echo = firstNonBlank(safeParams.get("echostr"), safeParams.get("challenge"));
        return StringUtils.isNotBlank(echo) ? echo : "ok";
    }

    /**
     * 接收第三方回调消息：
     * - JSON (application/json)
     * - form (application/x-www-form-urlencoded, multipart/form-data)
     * - raw text (text/plain, {@code *}/{@code *})
     */
    @PostMapping(value = "/msg", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<String> receive(@RequestBody(required = false) String body,
                                          @RequestParam(required = false) Map<String, String> params,
                                          @RequestHeader(required = false) Map<String, String> headers,
                                          HttpServletRequest request) {

        Map<String, String> safeParams = (params == null) ? Collections.emptyMap() : params;
        Map<String, String> safeHeaders = sanitizeHeaders(headers);

        String contentType = Objects.toString(request.getContentType(), "");
        String userAgent = Objects.toString(request.getHeader("User-Agent"), "");
        String safeBody = truncate(body, 10_000);

        logger.info("POST /msg callback received. remote={}, contentType={}, ua={}, params={}, headers={}, body={}",
                request.getRemoteAddr(),
                contentType,
                userAgent,
                JSON.toJSONString(safeParams),
                JSON.toJSONString(safeHeaders),
                safeBody);

        // 尝试从 body JSON 中抽取常见字段（仅用于日志定位，不做强校验）
        tryLogJsonHints(body);

        // 5秒内：相同 request content 去重（避免重复触发业务逻辑）
        String requestContent = extractContent(body);
        if (StringUtils.isNotBlank(requestContent)) {
            String h = sha256Hex(requestContent);
            if (isDuplicateWithinTtl(DEDUP_REQ_CONTENT, h, DEDUP_TTL_MS)) {
                logger.info("POST /msg dedup hit (request content). ttlMs={}, hash={}, content={}",
                        DEDUP_TTL_MS, h, truncate(requestContent, 200));
                String challenge = extractChallenge(body, safeParams);
                return ResponseEntity.ok(StringUtils.isNotBlank(challenge) ? challenge : "ok");
            }
        }

        // 仅当收到 #指令-天气 且 wx_id=cherfei0611 时，才触发天气接口
        if (containsWeatherCommand(body)) {
            try {
                String weatherResponse = getWeather(body);
                logger.info("POST /msg weather API called successfully. response={}", weatherResponse);
            } catch (Exception e) {
                logger.error("POST /msg weather API call failed", e);
            }
        }

        // 仅当收到 #指令-图片 且 wx_id=cherfei0611 时，才触发图片指令接口
        if (containsImageCommand(body)) {
            try {
                String imageResponse = sendImageCommand(body);
                logger.info("POST /msg image API called successfully. response={}", imageResponse);
            } catch (Exception e) {
                logger.error("POST /msg image API call failed", e);
            }
        }

        // 兼容回调握手/挑战（有些平台 POST JSON 返回 challenge）
        String challenge = extractChallenge(body, safeParams);
        return ResponseEntity.ok(StringUtils.isNotBlank(challenge) ? challenge : "ok");
    }

    private static void tryLogJsonHints(String body) {
        if (StringUtils.isBlank(body)) {
            return;
        }
        try {
            JSONObject json = JSONObject.parseObject(body);
            if (json == null || json.isEmpty()) {
                return;
            }
            String taskId = firstNonBlank(
                    json.getString("task_id"),
                    json.getString("taskId"),
                    json.getString("submit_id"),
                    json.getString("submitId"),
                    json.getString("history_id"),
                    json.getString("historyId")
            );
            String status = firstNonBlank(json.getString("status"), json.getString("state"));
            String type = firstNonBlank(json.getString("type"), json.getString("event"), json.getString("action"));

            if (StringUtils.isNotBlank(taskId) || StringUtils.isNotBlank(status) || StringUtils.isNotBlank(type)) {
                logger.info("POST /msg parsed hints. taskId={}, status={}, type={}", taskId, status, type);
            }
        } catch (Exception ignore) {
            // body 不是 JSON 或格式不标准，忽略
        }
    }

    private static String extractChallenge(String body, Map<String, String> params) {
        // query/form 优先
        String byParam = firstNonBlank(params.get("echostr"), params.get("challenge"));
        if (StringUtils.isNotBlank(byParam)) {
            return byParam;
        }
        if (StringUtils.isBlank(body)) {
            return null;
        }
        try {
            JSONObject json = JSONObject.parseObject(body);
            if (json == null) {
                return null;
            }
            return firstNonBlank(json.getString("echostr"), json.getString("challenge"));
        } catch (Exception ignore) {
            return null;
        }
    }

    /**
     * 过滤敏感 header（避免把 token / cookie 直接打到日志里）
     */
    private static Map<String, String> sanitizeHeaders(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> safe = new HashMap<>();
        for (Map.Entry<String, String> e : headers.entrySet()) {
            String k = e.getKey();
            if (k == null) {
                continue;
            }
            String keyLower = k.toLowerCase();
            if (keyLower.contains("authorization") || keyLower.contains("cookie") || keyLower.contains("token")) {
                safe.put(k, "***");
            } else {
                safe.put(k, truncate(e.getValue(), 512));
            }
        }
        return safe;
    }

    private static String truncate(String s, int maxLen) {
        if (s == null) {
            return null;
        }
        if (maxLen <= 0) {
            return "";
        }
        if (s.length() <= maxLen) {
            return s;
        }
        return s.substring(0, maxLen) + "...(truncated," + s.length() + ")";
    }

    private static String firstNonBlank(String... arr) {
        if (arr == null) {
            return null;
        }
        for (String s : arr) {
            if (StringUtils.isNotBlank(s)) {
                return s;
            }
        }
        return null;
    }

    /**
     * 触发条件：
     * - 回调 body JSON 中 wx_id 必须是 cherfei0611
     * - content/msg/message 必须包含 #指令-天气
     */
    private static boolean containsWeatherCommand(String body) {
        if (StringUtils.isBlank(body)) {
            return false;
        }
        try {
            JSONObject json = JSONObject.parseObject(body);
            if (json == null) {
                return false;
            }
            String wxId = firstNonBlank(
                    json.getString("wx_id"),
                    json.getString("wxId"),
                    json.getString("wxid")
            );
            if (!StringUtils.equals(wxId, WX_ID_WEATHER)) {
                return false;
            }
            String content = extractContent(json);
            return StringUtils.isNotBlank(content) && content.contains(WEATHER_COMMAND);
        } catch (Exception e) {
            // 非 JSON 则不触发（避免误触）
            return false;
        }
    }

    /**
     * 触发条件：
     * - 回调 body JSON 中 wx_id 必须是 cherfei0611
     * - content/msg/message 必须包含 #指令-图片
     */
    private static boolean containsImageCommand(String body) {
        if (StringUtils.isBlank(body)) {
            return false;
        }
        try {
            JSONObject json = JSONObject.parseObject(body);
            if (json == null) {
                return false;
            }
            String wxId = firstNonBlank(
                    json.getString("wx_id"),
                    json.getString("wxId"),
                    json.getString("wxid")
            );
            if (!StringUtils.equals(wxId, WX_ID_WEATHER)) {
                return false;
            }
            String content = extractContent(json);
            return StringUtils.isNotBlank(content) && content.contains(IMAGE_COMMAND);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 调用天气接口获取天气信息并发送
     *
     * @param callbackBody 回调消息的原始 body
     * @return 天气接口的响应内容
     */
    private static String getWeather(String callbackBody) {
        String apiUrl = "http://127.0.0.1:8989/api";
        
        try {
            // 强制使用指定 wx_id（按你的要求）
            String wxId = WX_ID_WEATHER;

            // 构造天气信息消息（这里可以根据实际需求获取真实天气数据）
            // 目前使用示例消息
            String weatherMsg = "2025年12月17日，星期三，上海今日天气信息是。\n" +
                    "天气状况：多云转晴\n" +
                    "气温：最高温度 13℃，最低温度 4℃\n" +
                    "实时气温：约为 12℃ 左右\n" +
                    "风力风向：北风 2-3 级\n" +
                    "湿度：约为 56% - 63%\n" +
                    "空气质量：良/轻度污染，建议佩戴口罩";

            // 构造请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("type", 7);
            requestBody.put("wx_id", wxId);
            requestBody.put("msg", weatherMsg);

            // 5秒内：相同 outbound request 去重（避免重复发送）
            String requestBodyStr = requestBody.toJSONString();
            if (isDuplicateWithinTtl(DEDUP_OUTBOUND_REQ, sha256Hex(requestBodyStr), DEDUP_TTL_MS)) {
                logger.info("url={}, requestBody={}, dedup=hit(ttlMs={})", apiUrl, requestBodyStr, DEDUP_TTL_MS);
                return "dedup_skipped";
            }

            // 5秒内：相同 response msg 去重（避免重复发送）
            String msgHash = sha256Hex(weatherMsg);
            if (isDuplicateWithinTtl(DEDUP_RESP_MSG, msgHash, DEDUP_TTL_MS)) {
                logger.info("url={}, requestBody={}, dedup=hit(ttlMs={}, msgHash={})",
                        apiUrl, requestBodyStr, DEDUP_TTL_MS, msgHash);
                return "dedup_skipped";
            }

            logger.info("url={}, requestBody={}", apiUrl, requestBodyStr);

            // 调用天气接口
            byte[] responseBytes = HttpUtil.postBytes(apiUrl, requestBodyStr);
            String response = new String(responseBytes, StandardCharsets.UTF_8);
            
            logger.info("POST /msg weather API response. response={}", response);
            return response;
            
        } catch (Exception e) {
            logger.error("POST /msg getWeather failed", e);
            throw new RuntimeException("调用天气接口失败: " + e.getMessage(), e);
        }
    }

    /**
     * 图片指令：POST http://127.0.0.1:8989/api
     *
     * <pre>
     * {
     *   "type": 8,
     *   "wx_id": "cherfei0611",
     *   "path": "C:\\Users\\Administrator\\Downloads\\xxx.png"
     * }
     * </pre>
     */
    private static String sendImageCommand(String callbackBody) {
        String apiUrl = "http://127.0.0.1:8989/api";
        try {
            String wxId = WX_ID_WEATHER; // 强制
            
            // 尝试从回调body中提取path
            String path = null;
            if (StringUtils.isNotBlank(callbackBody)) {
                try {
                    JSONObject callbackJson = JSONObject.parseObject(callbackBody);
                    path = extractPath(callbackJson);
                } catch (Exception e) {
                    logger.warn("Failed to parse callback body for path extraction", e);
                }
            }
            
            // 如果无法从回调中提取，使用默认路径
            if (StringUtils.isBlank(path)) {
                path = "C:\\Users\\Administrator\\Downloads\\生鲜海报-产品组合-原图002.png";
            }
            
            JSONObject requestBody = new JSONObject();
            requestBody.put("type", 8);
            requestBody.put("wx_id", wxId);
            requestBody.put("path", path);
            String requestBodyStr = requestBody.toJSONString();
            if (isDuplicateWithinTtl(DEDUP_OUTBOUND_REQ, sha256Hex(requestBodyStr), DEDUP_TTL_MS)) {
                logger.info("url={}, requestBody={}, dedup=hit(ttlMs={})", apiUrl, requestBodyStr, DEDUP_TTL_MS);
                return "dedup_skipped";
            }

            logger.info("url={}, requestBody={}", apiUrl, requestBodyStr);
            byte[] responseBytes = HttpUtil.postBytes(apiUrl, requestBodyStr);
            return new String(responseBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("POST /msg sendImageCommand failed", e);
            throw new RuntimeException("调用图片接口失败: " + e.getMessage(), e);
        }
    }

    private static String extractContent(String body) {
        if (StringUtils.isBlank(body)) {
            return null;
        }
        try {
            JSONObject json = JSONObject.parseObject(body);
            return extractContent(json);
        } catch (Exception ignore) {
            return null;
        }
    }

    private static String extractContent(JSONObject json) {
        if (json == null) {
            return null;
        }
        return firstNonBlank(
                json.getString("content"),
                json.getString("msg"),
                json.getString("message")
        );
    }

    /**
     * 优先读取回调 JSON 的 path 字段；否则尝试从 content 里解析：
     * "#指令-图片 <path>"
     */
    private static String extractPath(JSONObject callbackJson) {
        if (callbackJson == null) {
            return null;
        }
        String path = firstNonBlank(callbackJson.getString("path"), callbackJson.getString("file"), callbackJson.getString("filePath"));
        if (StringUtils.isNotBlank(path)) {
            return StringUtils.trim(path);
        }
        String content = extractContent(callbackJson);
        if (StringUtils.isBlank(content) || !content.contains(IMAGE_COMMAND)) {
            return null;
        }
        String after = StringUtils.substringAfter(content, IMAGE_COMMAND);
        after = StringUtils.trimToEmpty(after);
        // 支持 "#指令-图片:xxx" / "#指令-图片=xxx" / "#指令-图片 xxx"
        after = StringUtils.stripStart(after, ":= \t\r\n");
        return StringUtils.isBlank(after) ? null : after;
    }

    /**
     * 原子判重：同一个 hash 在 ttlMs 内重复出现，返回 true；否则写入 now 并返回 false。
     */
    private static boolean isDuplicateWithinTtl(ConcurrentHashMap<String, Long> cache, String hash, long ttlMs) {
        if (cache == null || StringUtils.isBlank(hash) || ttlMs <= 0) {
            return false;
        }
        long now = System.currentTimeMillis();
        maybeCleanup(now);
        final boolean[] dup = {false};
        cache.compute(hash, (k, last) -> {
            if (last != null && (now - last) < ttlMs) {
                dup[0] = true;
                return last;
            }
            return now;
        });
        return dup[0];
    }

    /**
     * 轻量清理：每隔 ttl 触发一次，移除过期项，避免缓存无限增长。
     */
    private static void maybeCleanup(long now) {
        long last = LAST_CLEANUP_MS.get();
        if ((now - last) < DEDUP_TTL_MS) {
            return;
        }
        if (!LAST_CLEANUP_MS.compareAndSet(last, now)) {
            return;
        }
        cleanupMap(DEDUP_REQ_CONTENT, now);
        cleanupMap(DEDUP_RESP_MSG, now);
        cleanupMap(DEDUP_OUTBOUND_REQ, now);
    }

    private static void cleanupMap(ConcurrentHashMap<String, Long> map, long now) {
        if (map == null || map.isEmpty()) {
            return;
        }
        map.entrySet().removeIf(e -> e.getValue() == null || (now - e.getValue()) >= DEDUP_TTL_MS);
    }

    private static String sha256Hex(String input) {
        if (input == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest);
        } catch (Exception e) {
            // 兜底：极少数情况下算法不可用/环境异常
            return String.valueOf(input.hashCode());
        }
    }

    /**
     * 将字节数组转换为十六进制字符串（Java 8 兼容）
     */
    private static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
}

