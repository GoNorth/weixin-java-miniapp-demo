package com.github.binarywang.demo.wx.miniapp.controller.receive;

import com.github.binarywang.demo.wx.miniapp.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMusicMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutNewsMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutTextMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutVideoMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutImageMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutVoiceMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 同步回复消息控制器（被动回复）
 * 参考文档：https://github.com/binarywang/WxJava/wiki/MP_%E5%90%8C%E6%AD%A5%E5%9B%9E%E5%A4%8D%E6%B6%88%E6%81%AF
 * 
 * 使用场景：如用户发送指令，如：[IMG1223]，则将指定日期的图片结果回复
 *
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */
@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/wx/reply/{appid}")
public class ReplyMessageController {
    private final WxMpService wxMpService;

    /**
     * <pre>
     * 回复文本消息
     * </pre>
     */
    @PostMapping("/text")
    public String replyTextMessage(@PathVariable String appid,
                                   @RequestBody ReplyTextRequest request) {
        try {
            validateAppid(appid);

            if (StringUtils.isBlank(request.getToUser())) {
                throw new IllegalArgumentException("toUser参数不能为空！");
            }
            if (StringUtils.isBlank(request.getFromUser())) {
                throw new IllegalArgumentException("fromUser参数不能为空！");
            }
            if (StringUtils.isBlank(request.getContent())) {
                throw new IllegalArgumentException("content参数不能为空！");
            }

            log.info("开始回复文本消息，appid={}, toUser={}, fromUser={}", appid, request.getToUser(), request.getFromUser());

            WxMpXmlOutTextMessage message = WxMpXmlOutMessage
                .TEXT()
                .content(request.getContent())
                .fromUser(request.getFromUser())
                .toUser(request.getToUser())
                .build();

            String xml = message.toXml();
            log.info("回复文本消息XML: {}", xml);

            return xml;
        } catch (Exception e) {
            log.error("回复文本消息异常: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());
            return JsonUtils.toJson(errorResult);
        }
    }

    /**
     * <pre>
     * 回复图片消息
     * </pre>
     */
    @PostMapping("/image")
    public String replyImageMessage(@PathVariable String appid,
                                    @RequestBody ReplyImageRequest request) {
        try {
            validateAppid(appid);

            if (StringUtils.isBlank(request.getToUser())) {
                throw new IllegalArgumentException("toUser参数不能为空！");
            }
            if (StringUtils.isBlank(request.getFromUser())) {
                throw new IllegalArgumentException("fromUser参数不能为空！");
            }
            if (StringUtils.isBlank(request.getMediaId())) {
                throw new IllegalArgumentException("mediaId参数不能为空！");
            }

            log.info("开始回复图片消息，appid={}, toUser={}, fromUser={}, mediaId={}", 
                appid, request.getToUser(), request.getFromUser(), request.getMediaId());

            WxMpXmlOutImageMessage message = WxMpXmlOutMessage
                .IMAGE()
                .mediaId(request.getMediaId())
                .fromUser(request.getFromUser())
                .toUser(request.getToUser())
                .build();

            String xml = message.toXml();
            log.info("回复图片消息XML: {}", xml);

            return xml;
        } catch (Exception e) {
            log.error("回复图片消息异常: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());
            return JsonUtils.toJson(errorResult);
        }
    }

    /**
     * <pre>
     * 回复语音消息
     * </pre>
     */
    @PostMapping("/voice")
    public String replyVoiceMessage(@PathVariable String appid,
                                    @RequestBody ReplyVoiceRequest request) {
        try {
            validateAppid(appid);

            if (StringUtils.isBlank(request.getToUser())) {
                throw new IllegalArgumentException("toUser参数不能为空！");
            }
            if (StringUtils.isBlank(request.getFromUser())) {
                throw new IllegalArgumentException("fromUser参数不能为空！");
            }
            if (StringUtils.isBlank(request.getMediaId())) {
                throw new IllegalArgumentException("mediaId参数不能为空！");
            }

            log.info("开始回复语音消息，appid={}, toUser={}, fromUser={}, mediaId={}", 
                appid, request.getToUser(), request.getFromUser(), request.getMediaId());

            WxMpXmlOutVoiceMessage message = WxMpXmlOutMessage
                .VOICE()
                .mediaId(request.getMediaId())
                .fromUser(request.getFromUser())
                .toUser(request.getToUser())
                .build();

            String xml = message.toXml();
            log.info("回复语音消息XML: {}", xml);

            return xml;
        } catch (Exception e) {
            log.error("回复语音消息异常: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());
            return JsonUtils.toJson(errorResult);
        }
    }

    /**
     * <pre>
     * 回复视频消息
     * </pre>
     */
    @PostMapping("/video")
    public String replyVideoMessage(@PathVariable String appid,
                                    @RequestBody ReplyVideoRequest request) {
        try {
            validateAppid(appid);

            if (StringUtils.isBlank(request.getToUser())) {
                throw new IllegalArgumentException("toUser参数不能为空！");
            }
            if (StringUtils.isBlank(request.getFromUser())) {
                throw new IllegalArgumentException("fromUser参数不能为空！");
            }
            if (StringUtils.isBlank(request.getMediaId())) {
                throw new IllegalArgumentException("mediaId参数不能为空！");
            }

            log.info("开始回复视频消息，appid={}, toUser={}, fromUser={}, mediaId={}", 
                appid, request.getToUser(), request.getFromUser(), request.getMediaId());

            WxMpXmlOutVideoMessage message = WxMpXmlOutMessage
                .VIDEO()
                .mediaId(request.getMediaId())
                .title(request.getTitle())
                .description(request.getDescription())
                .fromUser(request.getFromUser())
                .toUser(request.getToUser())
                .build();

            String xml = message.toXml();
            log.info("回复视频消息XML: {}", xml);

            return xml;
        } catch (Exception e) {
            log.error("回复视频消息异常: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());
            return JsonUtils.toJson(errorResult);
        }
    }

    /**
     * <pre>
     * 回复音乐消息
     * </pre>
     */
    @PostMapping("/music")
    public String replyMusicMessage(@PathVariable String appid,
                                    @RequestBody ReplyMusicRequest request) {
        try {
            validateAppid(appid);

            if (StringUtils.isBlank(request.getToUser())) {
                throw new IllegalArgumentException("toUser参数不能为空！");
            }
            if (StringUtils.isBlank(request.getFromUser())) {
                throw new IllegalArgumentException("fromUser参数不能为空！");
            }
            if (StringUtils.isBlank(request.getMusicUrl())) {
                throw new IllegalArgumentException("musicUrl参数不能为空！");
            }

            log.info("开始回复音乐消息，appid={}, toUser={}, fromUser={}", 
                appid, request.getToUser(), request.getFromUser());

            WxMpXmlOutMusicMessage message = WxMpXmlOutMessage
                .MUSIC()
                .musicUrl(request.getMusicUrl())
                .hqMusicUrl(request.getHqMusicUrl())
                .thumbMediaId(request.getThumbMediaId())
                .title(request.getTitle())
                .description(request.getDescription())
                .fromUser(request.getFromUser())
                .toUser(request.getToUser())
                .build();

            String xml = message.toXml();
            log.info("回复音乐消息XML: {}", xml);

            return xml;
        } catch (Exception e) {
            log.error("回复音乐消息异常: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());
            return JsonUtils.toJson(errorResult);
        }
    }

    /**
     * <pre>
     * 回复图文消息
     * </pre>
     */
    @PostMapping("/news")
    public String replyNewsMessage(@PathVariable String appid,
                                   @RequestBody ReplyNewsRequest request) {
        try {
            validateAppid(appid);

            if (StringUtils.isBlank(request.getToUser())) {
                throw new IllegalArgumentException("toUser参数不能为空！");
            }
            if (StringUtils.isBlank(request.getFromUser())) {
                throw new IllegalArgumentException("fromUser参数不能为空！");
            }
            if (request.getArticles() == null || request.getArticles().isEmpty()) {
                throw new IllegalArgumentException("articles参数不能为空！");
            }

            log.info("开始回复图文消息，appid={}, toUser={}, fromUser={}, 文章数量={}", 
                appid, request.getToUser(), request.getFromUser(), request.getArticles().size());

            WxMpXmlOutNewsMessage message = WxMpXmlOutMessage
                .NEWS()
                .fromUser(request.getFromUser())
                .toUser(request.getToUser())
                .build();

            // 添加文章
            for (ReplyNewsArticle article : request.getArticles()) {
                WxMpXmlOutNewsMessage.Item item = new WxMpXmlOutNewsMessage.Item();
                item.setTitle(article.getTitle());
                item.setDescription(article.getDescription());
                item.setPicUrl(article.getPicUrl());
                item.setUrl(article.getUrl());
                message.addArticle(item);
            }

            String xml = message.toXml();
            log.info("回复图文消息XML: {}", xml);

            return xml;
        } catch (Exception e) {
            log.error("回复图文消息异常: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());
            return JsonUtils.toJson(errorResult);
        }
    }

    /**
     * <pre>
     * 智能回复：根据接收到的消息内容自动回复
     * 示例：用户发送 [IMG1223]，则回复指定日期的图片
     * </pre>
     */
    @PostMapping("/smartReply")
    public String smartReply(@PathVariable String appid,
                             @RequestBody SmartReplyRequest request) {
        try {
            validateAppid(appid);

            if (StringUtils.isBlank(request.getToUser())) {
                throw new IllegalArgumentException("toUser参数不能为空！");
            }
            if (StringUtils.isBlank(request.getFromUser())) {
                throw new IllegalArgumentException("fromUser参数不能为空！");
            }
            if (StringUtils.isBlank(request.getContent())) {
                throw new IllegalArgumentException("content参数不能为空！");
            }

            log.info("智能回复，appid={}, toUser={}, fromUser={}, content={}", 
                appid, request.getToUser(), request.getFromUser(), request.getContent());

            String content = request.getContent().trim();
            WxMpXmlOutMessage message = null;

            // 示例：处理 [IMG1223] 格式的指令
            if (content.matches("\\[IMG\\d{4}\\]")) {
                // 提取日期，例如 [IMG1223] -> 1223
                String date = content.substring(4, 8);
                log.info("检测到图片指令，日期: {}", date);
                
                // 这里可以根据日期查询对应的图片mediaId
                // 示例：返回文本消息提示
                message = WxMpXmlOutMessage
                    .TEXT()
                    .content(String.format("您查询的是 %s 的图片，正在为您准备...", date))
                    .fromUser(request.getFromUser())
                    .toUser(request.getToUser())
                    .build();
            } else {
                // 默认回复
                message = WxMpXmlOutMessage
                    .TEXT()
                    .content("收到您的消息：" + content)
                    .fromUser(request.getFromUser())
                    .toUser(request.getToUser())
                    .build();
            }

            String xml = message.toXml();
            log.info("智能回复XML: {}", xml);

            return xml;
        } catch (Exception e) {
            log.error("智能回复异常: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());
            return JsonUtils.toJson(errorResult);
        }
    }

    /**
     * <pre>
     * 组合回复：先同步回复消息，然后发送模板消息
     * </pre>
     */
    @PostMapping("/replyWithTemplate")
    public String replyWithTemplate(@PathVariable String appid,
                                    @RequestBody ReplyWithTemplateRequest request) {
        try {
            validateAppid(appid);

            Map<String, Object> result = new HashMap<>();
            String replyXml = null;

            // 1. 先构建同步回复消息
            if (request.getReplyMessage() != null) {
                ReplyMessage replyMsg = request.getReplyMessage();
                
                switch (replyMsg.getMsgType().toLowerCase()) {
                    case "text":
                        WxMpXmlOutTextMessage textMsg = WxMpXmlOutMessage
                            .TEXT()
                            .content(replyMsg.getContent())
                            .fromUser(replyMsg.getFromUser())
                            .toUser(replyMsg.getToUser())
                            .build();
                        replyXml = textMsg.toXml();
                        break;
                    case "image":
                        WxMpXmlOutImageMessage imgMsg = WxMpXmlOutMessage
                            .IMAGE()
                            .mediaId(replyMsg.getMediaId())
                            .fromUser(replyMsg.getFromUser())
                            .toUser(replyMsg.getToUser())
                            .build();
                        replyXml = imgMsg.toXml();
                        break;
                    default:
                        throw new IllegalArgumentException("不支持的回复消息类型: " + replyMsg.getMsgType());
                }
                log.info("同步回复消息构建成功");
            }

            // 2. 发送模板消息（异步，不阻塞回复）
            if (request.getTemplateMessage() != null) {
                try {
                    com.github.binarywang.demo.wx.miniapp.controller.send.SendMessageWithTemplateController.TemplateMessageRequest templateMsg = 
                        request.getTemplateMessage();
                    
                    me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage templateMessage = 
                        me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage.builder()
                        .toUser(templateMsg.getToUser())
                        .templateId(templateMsg.getTemplateId())
                        .url(templateMsg.getUrl())
                        .miniProgram(templateMsg.getMiniProgram())
                        .build();

                    List<me.chanjar.weixin.mp.bean.template.WxMpTemplateData> templateDataList = new ArrayList<>();
                    for (Map.Entry<String, String> entry : templateMsg.getData().entrySet()) {
                        templateDataList.add(new me.chanjar.weixin.mp.bean.template.WxMpTemplateData(
                            entry.getKey(), entry.getValue(), templateMsg.getColor()));
                    }
                    templateMessage.setData(templateDataList);

                    String msgId = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
                    log.info("模板消息发送成功，msgId={}", msgId);
                } catch (Exception e) {
                    log.error("发送模板消息失败: {}", e.getMessage());
                }
            }

            // 返回同步回复的XML
            if (replyXml != null) {
                return replyXml;
            } else {
                result.put("success", true);
                result.put("message", "处理完成");
                return JsonUtils.toJson(result);
            }
        } catch (Exception e) {
            log.error("组合回复异常: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());
            return JsonUtils.toJson(errorResult);
        }
    }

    /**
     * 验证appid并切换配置
     */
    private void validateAppid(String appid) {
        if (StringUtils.isBlank(appid)) {
            throw new IllegalArgumentException("appid参数不能为空！");
        }

        if (!wxMpService.switchover(appid)) {
            throw new IllegalArgumentException(String.format("未找到对应appid=[%s]的公众号配置，请检查application.yml中的wx.mp.configs配置，确保appId已正确填写！", appid));
        }

        if (wxMpService.getWxMpConfigStorage() == null ||
            StringUtils.isBlank(wxMpService.getWxMpConfigStorage().getAppId())) {
            throw new IllegalArgumentException(String.format("公众号配置加载失败，appid=[%s]的配置中appId为空，请在application.yml中填写正确的appId！", appid));
        }
    }

    // ========== 请求实体类 ==========

    @Data
    public static class ReplyTextRequest {
        private String toUser;
        private String fromUser;
        private String content;
    }

    @Data
    public static class ReplyImageRequest {
        private String toUser;
        private String fromUser;
        private String mediaId;
    }

    @Data
    public static class ReplyVoiceRequest {
        private String toUser;
        private String fromUser;
        private String mediaId;
    }

    @Data
    public static class ReplyVideoRequest {
        private String toUser;
        private String fromUser;
        private String mediaId;
        private String title;
        private String description;
    }

    @Data
    public static class ReplyMusicRequest {
        private String toUser;
        private String fromUser;
        private String musicUrl;
        private String hqMusicUrl;
        private String thumbMediaId;
        private String title;
        private String description;
    }

    @Data
    public static class ReplyNewsRequest {
        private String toUser;
        private String fromUser;
        private List<ReplyNewsArticle> articles;
    }

    @Data
    public static class ReplyNewsArticle {
        private String title;
        private String description;
        private String picUrl;
        private String url;
    }

    @Data
    public static class SmartReplyRequest {
        private String toUser;
        private String fromUser;
        private String content;
    }

    @Data
    public static class ReplyMessage {
        private String msgType; // text, image
        private String toUser;
        private String fromUser;
        private String content; // text消息使用
        private String mediaId; // image消息使用
    }

    @Data
    public static class ReplyWithTemplateRequest {
        private ReplyMessage replyMessage;
        private com.github.binarywang.demo.wx.miniapp.controller.send.SendMessageWithTemplateController.TemplateMessageRequest templateMessage;
    }
}
