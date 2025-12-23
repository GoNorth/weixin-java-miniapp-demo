package com.github.binarywang.demo.wx.miniapp.controller.send;

import com.github.binarywang.demo.wx.miniapp.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 发送模板消息控制器
 * 参考文档：https://github.com/binarywang/WxJava/wiki/MP_%E5%8F%91%E9%80%81%E6%A8%A1%E6%9D%BF%E6%B6%88%E6%81%AF
 *
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */
@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/wx/template/{appid}")
public class SendMessageWithTemplateController {
    private final WxMpService wxMpService;

    /**
     * <pre>
     * 发送模板消息
     * </pre>
     */
    @PostMapping("/send")
    public String sendTemplateMessage(@PathVariable String appid,
                                      @RequestBody TemplateMessageRequest request) {
        try {
            // 切换公众号配置
            if (StringUtils.isBlank(appid)) {
                throw new IllegalArgumentException("appid参数不能为空！");
            }

            if (!wxMpService.switchover(appid)) {
                throw new IllegalArgumentException(String.format("未找到对应appid=[%s]的公众号配置，请检查application.yml中的wx.mp.configs配置，确保appId已正确填写！", appid));
            }

            // 验证配置是否正确加载
            if (wxMpService.getWxMpConfigStorage() == null ||
                StringUtils.isBlank(wxMpService.getWxMpConfigStorage().getAppId())) {
                throw new IllegalArgumentException(String.format("公众号配置加载失败，appid=[%s]的配置中appId为空，请在application.yml中填写正确的appId！", appid));
            }

            // 参数验证
            if (StringUtils.isBlank(request.getToUser())) {
                throw new IllegalArgumentException("toUser参数不能为空！");
            }
            if (StringUtils.isBlank(request.getTemplateId())) {
                throw new IllegalArgumentException("templateId参数不能为空！");
            }
            if (request.getData() == null || request.getData().isEmpty()) {
                throw new IllegalArgumentException("data参数不能为空！");
            }

            log.info("开始发送模板消息，appid={}, toUser={}, templateId={}", appid, request.getToUser(), request.getTemplateId());

            // 构建模板消息
            WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .toUser(request.getToUser())
                .templateId(request.getTemplateId())
                .url(request.getUrl())
                .miniProgram(request.getMiniProgram())
                .build();

            // 添加模板数据
            List<WxMpTemplateData> templateDataList = new ArrayList<>();
            for (Map.Entry<String, String> entry : request.getData().entrySet()) {
                templateDataList.add(new WxMpTemplateData(entry.getKey(), entry.getValue(), request.getColor()));
            }
            templateMessage.setData(templateDataList);

            log.info("模板消息内容: {}", JsonUtils.toJson(templateMessage));

            // 发送模板消息
            String msgId = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);

            log.info("模板消息发送成功，msgId={}", msgId);

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> data = new HashMap<>();
            data.put("msgId", msgId);
            data.put("toUser", request.getToUser());
            data.put("templateId", request.getTemplateId());
            result.put("data", data);
            result.put("success", true);
            result.put("message", "模板消息发送成功");

            return JsonUtils.toJson(result);
        } catch (WxErrorException e) {
            log.error("发送模板消息失败: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());
            return JsonUtils.toJson(errorResult);
        } catch (Exception e) {
            log.error("发送模板消息异常: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());
            return JsonUtils.toJson(errorResult);
        }
    }

    /**
     * <pre>
     * 批量发送模板消息
     * </pre>
     */
    @PostMapping("/batchSend")
    public String batchSendTemplateMessage(@PathVariable String appid,
                                           @RequestBody BatchTemplateMessageRequest request) {
        try {
            // 切换公众号配置
            if (StringUtils.isBlank(appid)) {
                throw new IllegalArgumentException("appid参数不能为空！");
            }

            if (!wxMpService.switchover(appid)) {
                throw new IllegalArgumentException(String.format("未找到对应appid=[%s]的公众号配置，请检查application.yml中的wx.mp.configs配置，确保appId已正确填写！", appid));
            }

            // 验证配置是否正确加载
            if (wxMpService.getWxMpConfigStorage() == null ||
                StringUtils.isBlank(wxMpService.getWxMpConfigStorage().getAppId())) {
                throw new IllegalArgumentException(String.format("公众号配置加载失败，appid=[%s]的配置中appId为空，请在application.yml中填写正确的appId！", appid));
            }

            // 参数验证
            if (request.getMessages() == null || request.getMessages().isEmpty()) {
                throw new IllegalArgumentException("messages参数不能为空！");
            }

            log.info("开始批量发送模板消息，appid={}, 消息数量={}", appid, request.getMessages().size());

            List<Map<String, Object>> results = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;

            for (TemplateMessageRequest msgRequest : request.getMessages()) {
                Map<String, Object> result = new HashMap<>();
                try {
                    // 参数验证
                    if (StringUtils.isBlank(msgRequest.getToUser())) {
                        throw new IllegalArgumentException("toUser参数不能为空！");
                    }
                    if (StringUtils.isBlank(msgRequest.getTemplateId())) {
                        throw new IllegalArgumentException("templateId参数不能为空！");
                    }
                    if (msgRequest.getData() == null || msgRequest.getData().isEmpty()) {
                        throw new IllegalArgumentException("data参数不能为空！");
                    }

                    // 构建模板消息
                    WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                        .toUser(msgRequest.getToUser())
                        .templateId(msgRequest.getTemplateId())
                        .url(msgRequest.getUrl())
                        .miniProgram(msgRequest.getMiniProgram())
                        .build();

                    // 添加模板数据
                    List<WxMpTemplateData> templateDataList = new ArrayList<>();
                    for (Map.Entry<String, String> entry : msgRequest.getData().entrySet()) {
                        templateDataList.add(new WxMpTemplateData(entry.getKey(), entry.getValue(), msgRequest.getColor()));
                    }
                    templateMessage.setData(templateDataList);

                    // 发送模板消息
                    String msgId = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);

                    result.put("success", true);
                    result.put("toUser", msgRequest.getToUser());
                    result.put("msgId", msgId);
                    result.put("message", "发送成功");
                    successCount++;
                } catch (WxErrorException e) {
                    log.error("发送模板消息失败，toUser={}: {}", msgRequest.getToUser(), e.getMessage());
                    result.put("success", false);
                    result.put("toUser", msgRequest.getToUser());
                    result.put("error", e.getMessage());
                    failCount++;
                } catch (Exception e) {
                    log.error("发送模板消息异常，toUser={}: {}", msgRequest.getToUser(), e.getMessage());
                    result.put("success", false);
                    result.put("toUser", msgRequest.getToUser());
                    result.put("error", e.getMessage());
                    failCount++;
                }
                results.add(result);
            }

            // 构建返回结果
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> data = new HashMap<>();
            data.put("results", results);
            data.put("total", request.getMessages().size());
            data.put("successCount", successCount);
            data.put("failCount", failCount);
            response.put("data", data);
            response.put("success", true);

            log.info("批量发送模板消息完成，成功={}, 失败={}", successCount, failCount);

            return JsonUtils.toJson(response);
        } catch (Exception e) {
            log.error("批量发送模板消息异常: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());
            return JsonUtils.toJson(errorResult);
        }
    }

    /**
     * 模板消息请求实体类
     */
    @Data
    public static class TemplateMessageRequest {
        /**
         * 接收者openid
         */
        private String toUser;

        /**
         * 模板ID
         */
        private String templateId;

        /**
         * 模板跳转链接（可选）
         */
        private String url;

        /**
         * 跳小程序所需数据（可选）
         */
        private WxMpTemplateMessage.MiniProgram miniProgram;

        /**
         * 模板数据
         * key: 模板字段名（如：keyword1, keyword2等）
         * value: 模板字段值
         */
        private Map<String, String> data;

        /**
         * 模板内容字体颜色（可选）
         */
        private String color;
    }

    /**
     * 批量模板消息请求实体类
     */
    @Data
    public static class BatchTemplateMessageRequest {
        /**
         * 模板消息列表
         */
        private List<TemplateMessageRequest> messages;
    }
}
