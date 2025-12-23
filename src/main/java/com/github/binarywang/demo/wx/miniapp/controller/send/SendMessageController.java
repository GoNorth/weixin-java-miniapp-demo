package com.github.binarywang.demo.wx.miniapp.controller.send;

import com.github.binarywang.demo.wx.miniapp.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 主动发送消息控制器（客服消息）
 * 参考文档：https://github.com/binarywang/WxJava/wiki/MP_%E4%B8%BB%E5%8A%A8%E5%8F%91%E9%80%81%E6%B6%88%E6%81%AF%EF%BC%88%E5%AE%A2%E6%9C%8D%E6%B6%88%E6%81%AF%EF%BC%89
 *
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */
@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/wx/kefu/{appid}")
public class SendMessageController {
    private final WxMpService wxMpService;

    /**
     * <pre>
     * 发送文本消息
     * </pre>
     */
    @PostMapping("/sendText")
    public String sendTextMessage(@PathVariable String appid,
                                  @RequestBody TextMessageRequest request) {
        try {
            validateAppid(appid);

            if (StringUtils.isBlank(request.getToUser())) {
                throw new IllegalArgumentException("toUser参数不能为空！");
            }
            if (StringUtils.isBlank(request.getContent())) {
                throw new IllegalArgumentException("content参数不能为空！");
            }

            log.info("开始发送文本消息，appid={}, toUser={}", appid, request.getToUser());

            WxMpKefuMessage message = WxMpKefuMessage
                .TEXT()
                .toUser(request.getToUser())
                .content(request.getContent())
                .build();

            wxMpService.getKefuService().sendKefuMessage(message);

            log.info("文本消息发送成功");

            return buildSuccessResult("文本消息发送成功");
        } catch (WxErrorException e) {
            log.error("发送文本消息失败: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        } catch (Exception e) {
            log.error("发送文本消息异常: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        }
    }

    /**
     * <pre>
     * 发送图片消息
     * </pre>
     */
    @PostMapping("/sendImage")
    public String sendImageMessage(@PathVariable String appid,
                                   @RequestBody ImageMessageRequest request) {
        try {
            validateAppid(appid);

            if (StringUtils.isBlank(request.getToUser())) {
                throw new IllegalArgumentException("toUser参数不能为空！");
            }
            if (StringUtils.isBlank(request.getMediaId())) {
                throw new IllegalArgumentException("mediaId参数不能为空！");
            }

            log.info("开始发送图片消息，appid={}, toUser={}, mediaId={}", appid, request.getToUser(), request.getMediaId());

            WxMpKefuMessage message = WxMpKefuMessage
                .IMAGE()
                .toUser(request.getToUser())
                .mediaId(request.getMediaId())
                .build();

            wxMpService.getKefuService().sendKefuMessage(message);

            log.info("图片消息发送成功");

            return buildSuccessResult("图片消息发送成功");
        } catch (WxErrorException e) {
            log.error("发送图片消息失败: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        } catch (Exception e) {
            log.error("发送图片消息异常: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        }
    }

    /**
     * <pre>
     * 发送语音消息
     * </pre>
     */
    @PostMapping("/sendVoice")
    public String sendVoiceMessage(@PathVariable String appid,
                                   @RequestBody VoiceMessageRequest request) {
        try {
            validateAppid(appid);

            if (StringUtils.isBlank(request.getToUser())) {
                throw new IllegalArgumentException("toUser参数不能为空！");
            }
            if (StringUtils.isBlank(request.getMediaId())) {
                throw new IllegalArgumentException("mediaId参数不能为空！");
            }

            log.info("开始发送语音消息，appid={}, toUser={}, mediaId={}", appid, request.getToUser(), request.getMediaId());

            WxMpKefuMessage message = WxMpKefuMessage
                .VOICE()
                .toUser(request.getToUser())
                .mediaId(request.getMediaId())
                .build();

            wxMpService.getKefuService().sendKefuMessage(message);

            log.info("语音消息发送成功");

            return buildSuccessResult("语音消息发送成功");
        } catch (WxErrorException e) {
            log.error("发送语音消息失败: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        } catch (Exception e) {
            log.error("发送语音消息异常: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        }
    }

    /**
     * <pre>
     * 发送视频消息
     * </pre>
     */
    @PostMapping("/sendVideo")
    public String sendVideoMessage(@PathVariable String appid,
                                   @RequestBody VideoMessageRequest request) {
        try {
            validateAppid(appid);

            if (StringUtils.isBlank(request.getToUser())) {
                throw new IllegalArgumentException("toUser参数不能为空！");
            }
            if (StringUtils.isBlank(request.getMediaId())) {
                throw new IllegalArgumentException("mediaId参数不能为空！");
            }

            log.info("开始发送视频消息，appid={}, toUser={}, mediaId={}", appid, request.getToUser(), request.getMediaId());

            WxMpKefuMessage message = WxMpKefuMessage
                .VIDEO()
                .toUser(request.getToUser())
                .mediaId(request.getMediaId())
                .thumbMediaId(request.getThumbMediaId())
                .title(request.getTitle())
                .description(request.getDescription())
                .build();

            wxMpService.getKefuService().sendKefuMessage(message);

            log.info("视频消息发送成功");

            return buildSuccessResult("视频消息发送成功");
        } catch (WxErrorException e) {
            log.error("发送视频消息失败: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        } catch (Exception e) {
            log.error("发送视频消息异常: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        }
    }

    /**
     * <pre>
     * 发送音乐消息
     * </pre>
     */
    @PostMapping("/sendMusic")
    public String sendMusicMessage(@PathVariable String appid,
                                   @RequestBody MusicMessageRequest request) {
        try {
            validateAppid(appid);

            if (StringUtils.isBlank(request.getToUser())) {
                throw new IllegalArgumentException("toUser参数不能为空！");
            }
            if (StringUtils.isBlank(request.getMusicUrl())) {
                throw new IllegalArgumentException("musicUrl参数不能为空！");
            }

            log.info("开始发送音乐消息，appid={}, toUser={}", appid, request.getToUser());

            WxMpKefuMessage message = WxMpKefuMessage
                .MUSIC()
                .toUser(request.getToUser())
                .musicUrl(request.getMusicUrl())
                .hqMusicUrl(request.getHqMusicUrl())
                .thumbMediaId(request.getThumbMediaId())
                .title(request.getTitle())
                .description(request.getDescription())
                .build();

            wxMpService.getKefuService().sendKefuMessage(message);

            log.info("音乐消息发送成功");

            return buildSuccessResult("音乐消息发送成功");
        } catch (WxErrorException e) {
            log.error("发送音乐消息失败: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        } catch (Exception e) {
            log.error("发送音乐消息异常: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        }
    }

    /**
     * <pre>
     * 发送图文消息（点击跳转到外链）
     * </pre>
     */
    @PostMapping("/sendNews")
    public String sendNewsMessage(@PathVariable String appid,
                                  @RequestBody NewsMessageRequest request) {
        try {
            validateAppid(appid);

            if (StringUtils.isBlank(request.getToUser())) {
                throw new IllegalArgumentException("toUser参数不能为空！");
            }
            if (request.getArticles() == null || request.getArticles().isEmpty()) {
                throw new IllegalArgumentException("articles参数不能为空！");
            }

            log.info("开始发送图文消息，appid={}, toUser={}, 文章数量={}", appid, request.getToUser(), request.getArticles().size());

            WxMpKefuMessage.WxArticle article = new WxMpKefuMessage.WxArticle();
            List<WxMpKefuMessage.WxArticle> articles = new ArrayList<>();

            for (NewsArticle articleData : request.getArticles()) {
                article = new WxMpKefuMessage.WxArticle();
                article.setTitle(articleData.getTitle());
                article.setDescription(articleData.getDescription());
                article.setPicUrl(articleData.getPicUrl());
                article.setUrl(articleData.getUrl());
                articles.add(article);
            }

            WxMpKefuMessage message = WxMpKefuMessage
                .NEWS()
                .toUser(request.getToUser())
                .articles(articles)
                .build();

            wxMpService.getKefuService().sendKefuMessage(message);

            log.info("图文消息发送成功");

            return buildSuccessResult("图文消息发送成功");
        } catch (WxErrorException e) {
            log.error("发送图文消息失败: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        } catch (Exception e) {
            log.error("发送图文消息异常: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        }
    }

    /**
     * <pre>
     * 发送图文消息（点击跳转到图文消息页面）
     * </pre>
     */
    @PostMapping("/sendMpNews")
    public String sendMpNewsMessage(@PathVariable String appid,
                                    @RequestBody MpNewsMessageRequest request) {
        try {
            validateAppid(appid);

            if (StringUtils.isBlank(request.getToUser())) {
                throw new IllegalArgumentException("toUser参数不能为空！");
            }
            if (StringUtils.isBlank(request.getMediaId())) {
                throw new IllegalArgumentException("mediaId参数不能为空！");
            }

            log.info("开始发送图文消息（mpnews），appid={}, toUser={}, mediaId={}", appid, request.getToUser(), request.getMediaId());

            WxMpKefuMessage message = WxMpKefuMessage
                .MPNEWS()
                .toUser(request.getToUser())
                .mediaId(request.getMediaId())
                .build();

            wxMpService.getKefuService().sendKefuMessage(message);

            log.info("图文消息（mpnews）发送成功");

            return buildSuccessResult("图文消息（mpnews）发送成功");
        } catch (WxErrorException e) {
            log.error("发送图文消息（mpnews）失败: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        } catch (Exception e) {
            log.error("发送图文消息（mpnews）异常: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        }
    }

    /**
     * <pre>
     * 发送卡券消息
     * </pre>
     */
    @PostMapping("/sendCard")
    public String sendCardMessage(@PathVariable String appid,
                                  @RequestBody CardMessageRequest request) {
        try {
            validateAppid(appid);

            if (StringUtils.isBlank(request.getToUser())) {
                throw new IllegalArgumentException("toUser参数不能为空！");
            }
            if (StringUtils.isBlank(request.getCardId())) {
                throw new IllegalArgumentException("cardId参数不能为空！");
            }

            log.info("开始发送卡券消息，appid={}, toUser={}, cardId={}", appid, request.getToUser(), request.getCardId());

            WxMpKefuMessage message = WxMpKefuMessage
                .WXCARD()
                .toUser(request.getToUser())
                .cardId(request.getCardId())
                .build();

            wxMpService.getKefuService().sendKefuMessage(message);

            log.info("卡券消息发送成功");

            return buildSuccessResult("卡券消息发送成功");
        } catch (WxErrorException e) {
            log.error("发送卡券消息失败: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        } catch (Exception e) {
            log.error("发送卡券消息异常: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        }
    }

    /**
     * <pre>
     * 发送小程序卡片消息
     * </pre>
     */
    @PostMapping("/sendMiniProgramPage")
    public String sendMiniProgramPageMessage(@PathVariable String appid,
                                             @RequestBody MiniProgramPageMessageRequest request) {
        try {
            validateAppid(appid);

            if (StringUtils.isBlank(request.getToUser())) {
                throw new IllegalArgumentException("toUser参数不能为空！");
            }
            if (StringUtils.isBlank(request.getTitle())) {
                throw new IllegalArgumentException("title参数不能为空！");
            }
            if (StringUtils.isBlank(request.getAppId())) {
                throw new IllegalArgumentException("appId参数不能为空！");
            }
            if (StringUtils.isBlank(request.getPagePath())) {
                throw new IllegalArgumentException("pagePath参数不能为空！");
            }
            if (StringUtils.isBlank(request.getThumbMediaId())) {
                throw new IllegalArgumentException("thumbMediaId参数不能为空！");
            }

            log.info("开始发送小程序卡片消息，appid={}, toUser={}", appid, request.getToUser());

            WxMpKefuMessage message = WxMpKefuMessage
                .MINIPROGRAMPAGE()
                .toUser(request.getToUser())
                .title(request.getTitle())
                .appId(request.getAppId())
                .pagePath(request.getPagePath())
                .thumbMediaId(request.getThumbMediaId())
                .build();

            wxMpService.getKefuService().sendKefuMessage(message);

            log.info("小程序卡片消息发送成功");

            return buildSuccessResult("小程序卡片消息发送成功");
        } catch (WxErrorException e) {
            log.error("发送小程序卡片消息失败: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        } catch (Exception e) {
            log.error("发送小程序卡片消息异常: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        }
    }

    /**
     * <pre>
     * 组合发送：先发送客服消息，再发送模板消息
     * </pre>
     */
    @PostMapping("/sendWithTemplate")
    public String sendWithTemplate(@PathVariable String appid,
                                   @RequestBody CombinedMessageRequest request) {
        try {
            validateAppid(appid);

            Map<String, Object> result = new HashMap<>();
            List<Map<String, Object>> results = new ArrayList<>();

            // 1. 先发送客服消息
            if (request.getKefuMessage() != null) {
                try {
                    KefuMessageRequest kefuMsg = request.getKefuMessage();
                    WxMpKefuMessage message = null;

                    switch (kefuMsg.getMsgType().toLowerCase()) {
                        case "text":
                            message = WxMpKefuMessage.TEXT()
                                .toUser(kefuMsg.getToUser())
                                .content(kefuMsg.getContent())
                                .build();
                            break;
                        case "image":
                            message = WxMpKefuMessage.IMAGE()
                                .toUser(kefuMsg.getToUser())
                                .mediaId(kefuMsg.getMediaId())
                                .build();
                            break;
                        case "voice":
                            message = WxMpKefuMessage.VOICE()
                                .toUser(kefuMsg.getToUser())
                                .mediaId(kefuMsg.getMediaId())
                                .build();
                            break;
                        default:
                            throw new IllegalArgumentException("不支持的客服消息类型: " + kefuMsg.getMsgType());
                    }

                    if (message != null) {
                        wxMpService.getKefuService().sendKefuMessage(message);
                        Map<String, Object> kefuResult = new HashMap<>();
                        kefuResult.put("type", "kefu");
                        kefuResult.put("success", true);
                        kefuResult.put("message", "客服消息发送成功");
                        results.add(kefuResult);
                        log.info("客服消息发送成功");
                    }
                } catch (Exception e) {
                    log.error("发送客服消息失败: {}", e.getMessage());
                    Map<String, Object> kefuResult = new HashMap<>();
                    kefuResult.put("type", "kefu");
                    kefuResult.put("success", false);
                    kefuResult.put("error", e.getMessage());
                    results.add(kefuResult);
                }
            }

            // 2. 再发送模板消息
            if (request.getTemplateMessage() != null) {
                try {
                    SendMessageWithTemplateController.TemplateMessageRequest templateMsg = request.getTemplateMessage();
                    
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
                    
                    Map<String, Object> templateResult = new HashMap<>();
                    templateResult.put("type", "template");
                    templateResult.put("success", true);
                    templateResult.put("msgId", msgId);
                    templateResult.put("message", "模板消息发送成功");
                    results.add(templateResult);
                    log.info("模板消息发送成功，msgId={}", msgId);
                } catch (Exception e) {
                    log.error("发送模板消息失败: {}", e.getMessage());
                    Map<String, Object> templateResult = new HashMap<>();
                    templateResult.put("type", "template");
                    templateResult.put("success", false);
                    templateResult.put("error", e.getMessage());
                    results.add(templateResult);
                }
            }

            result.put("success", true);
            result.put("results", results);
            return JsonUtils.toJson(result);
        } catch (Exception e) {
            log.error("组合发送消息异常: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
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

    /**
     * 构建成功结果
     */
    private String buildSuccessResult(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", message);
        return JsonUtils.toJson(result);
    }

    /**
     * 构建错误结果
     */
    private String buildErrorResult(String error) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", error);
        return JsonUtils.toJson(result);
    }

    // ========== 请求实体类 ==========

    @Data
    public static class TextMessageRequest {
        private String toUser;
        private String content;
    }

    @Data
    public static class ImageMessageRequest {
        private String toUser;
        private String mediaId;
    }

    @Data
    public static class VoiceMessageRequest {
        private String toUser;
        private String mediaId;
    }

    @Data
    public static class VideoMessageRequest {
        private String toUser;
        private String mediaId;
        private String thumbMediaId;
        private String title;
        private String description;
    }

    @Data
    public static class MusicMessageRequest {
        private String toUser;
        private String musicUrl;
        private String hqMusicUrl;
        private String thumbMediaId;
        private String title;
        private String description;
    }

    @Data
    public static class NewsMessageRequest {
        private String toUser;
        private List<NewsArticle> articles;
    }

    @Data
    public static class NewsArticle {
        private String title;
        private String description;
        private String picUrl;
        private String url;
    }

    @Data
    public static class MpNewsMessageRequest {
        private String toUser;
        private String mediaId;
    }

    @Data
    public static class CardMessageRequest {
        private String toUser;
        private String cardId;
    }

    @Data
    public static class MiniProgramPageMessageRequest {
        private String toUser;
        private String title;
        private String appId;
        private String pagePath;
        private String thumbMediaId;
    }

    @Data
    public static class KefuMessageRequest {
        private String msgType; // text, image, voice
        private String toUser;
        private String content; // text消息使用
        private String mediaId; // image/voice消息使用
    }

    @Data
    public static class CombinedMessageRequest {
        private KefuMessageRequest kefuMessage;
        private SendMessageWithTemplateController.TemplateMessageRequest templateMessage;
    }
}

