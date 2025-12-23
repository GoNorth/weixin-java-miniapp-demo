package com.github.binarywang.demo.wx.miniapp.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import cn.binarywang.wx.miniapp.util.WxMaConfigHolder;
import com.github.binarywang.demo.wx.miniapp.utils.JsonUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import me.chanjar.weixin.mp.bean.result.WxMpUserList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 微信小程序用户接口
 *
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */
@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/wx/user/{appid}")
public class WxMaUserController {
    private final WxMaService wxMaService;
    private final WxMpService wxMpService;

    /**
     * 登陆接口
     */
    @GetMapping("/login")
    public String login(@PathVariable String appid, String code) {
        if (StringUtils.isBlank(code)) {
            return "empty jscode";
        }

        if (!wxMaService.switchover(appid)) {
            throw new IllegalArgumentException(String.format("未找到对应appid=[%s]的配置，请核实！", appid));
        }

        try {
            WxMaJscode2SessionResult session = wxMaService.getUserService().getSessionInfo(code);
            log.info(session.getSessionKey());
            log.info(session.getOpenid());
            //TODO 可以增加自己的逻辑，关联业务相关数据
            return JsonUtils.toJson(session);
        } catch (WxErrorException e) {
            log.error(e.getMessage(), e);
            return e.toString();
        } finally {
            WxMaConfigHolder.remove();//清理ThreadLocal
        }
    }

    /**
     * <pre>
     * 获取用户信息接口
     * </pre>
     */
    @GetMapping("/info")
    public String info(@PathVariable String appid, String sessionKey,
                       String signature, String rawData, String encryptedData, String iv) {
        if (!wxMaService.switchover(appid)) {
            throw new IllegalArgumentException(String.format("未找到对应appid=[%s]的配置，请核实！", appid));
        }

        // 用户信息校验
        if (!wxMaService.getUserService().checkUserInfo(sessionKey, rawData, signature)) {
            WxMaConfigHolder.remove();//清理ThreadLocal
            return "user check failed";
        }

        // 解密用户信息
        WxMaUserInfo userInfo = wxMaService.getUserService().getUserInfo(sessionKey, encryptedData, iv);
        WxMaConfigHolder.remove();//清理ThreadLocal
        return JsonUtils.toJson(userInfo);
    }

    /**
     * <pre>
     * 获取用户绑定手机号信息
     * </pre>
     */
    @GetMapping("/phone")
    public String phone(@PathVariable String appid, String sessionKey, String signature,
                        String rawData, String encryptedData, String iv) {
        if (!wxMaService.switchover(appid)) {
            throw new IllegalArgumentException(String.format("未找到对应appid=[%s]的配置，请核实！", appid));
        }

        // 用户信息校验
        if (!wxMaService.getUserService().checkUserInfo(sessionKey, rawData, signature)) {
            WxMaConfigHolder.remove();//清理ThreadLocal
            return "user check failed";
        }

        // 解密
        WxMaPhoneNumberInfo phoneNoInfo = wxMaService.getUserService().getPhoneNoInfo(sessionKey, encryptedData, iv);
        WxMaConfigHolder.remove();//清理ThreadLocal
        return JsonUtils.toJson(phoneNoInfo);
    }

    /**
     * <pre>
     * 根据openid列表批量获取用户信息
     * </pre>
     */
    @GetMapping("/userInfoList")
    public String userInfoList(@PathVariable String appid,
                              @RequestParam String openids) {
        try {
            // 切换公众号配置
            if (StringUtils.isBlank(appid)) {
                throw new IllegalArgumentException("appid参数不能为空！");
            }

            if (StringUtils.isBlank(openids)) {
                throw new IllegalArgumentException("openids参数不能为空！");
            }

            if (!wxMpService.switchover(appid)) {
                throw new IllegalArgumentException(String.format("未找到对应appid=[%s]的公众号配置，请检查application.yml中的wx.mp.configs配置，确保appId已正确填写！", appid));
            }

            // 验证配置是否正确加载
            if (wxMpService.getWxMpConfigStorage() == null ||
                StringUtils.isBlank(wxMpService.getWxMpConfigStorage().getAppId())) {
                throw new IllegalArgumentException(String.format("公众号配置加载失败，appid=[%s]的配置中appId为空，请在application.yml中填写正确的appId！", appid));
            }

            // 解析openid列表（支持逗号分隔）
            List<String> openIdList = Arrays.stream(openids.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

            if (openIdList.isEmpty()) {
                throw new IllegalArgumentException("openids参数解析后为空，请确保格式正确（多个openid用逗号分隔）！");
            }

            log.info("开始批量获取用户信息，appid={}, openid数量={}", appid, openIdList.size());
            log.debug("openid列表: {}", JsonUtils.toJson(openIdList));

            List<UserInfo> userList = new ArrayList<>();

            // 批量获取用户信息（每次最多100个）
            int batchSize = 100;
            for (int i = 0; i < openIdList.size(); i += batchSize) {
                int end = Math.min(i + batchSize, openIdList.size());
                List<String> batchOpenIds = openIdList.subList(i, end);

                log.info("准备批量获取用户信息，批次索引: {}-{}, openid数量: {}", i, end - 1, batchOpenIds.size());
                log.debug("批次openid列表: {}", JsonUtils.toJson(batchOpenIds));

                // 批量获取用户信息
                log.info("调用微信API批量获取用户信息，openid列表: {}", JsonUtils.toJson(batchOpenIds));
                List<WxMpUser> wxMpUsers = wxMpService.getUserService().userInfoList(batchOpenIds);

                log.info("批量获取用户信息响应，批次大小: {}, 实际返回用户数: {}", batchOpenIds.size(), wxMpUsers != null ? wxMpUsers.size() : 0);

                // 打印整个批次响应的原始JSON
                if (wxMpUsers != null && !wxMpUsers.isEmpty()) {
                    try {
                        log.info("批次用户信息完整JSON响应: {}", JsonUtils.toJson(wxMpUsers));
                    } catch (Exception e) {
                        log.warn("无法序列化批次用户信息为JSON: {}", e.getMessage());
                    }
                }

                for (WxMpUser wxMpUser : wxMpUsers) {
                    // 打印原始用户信息的关键字段
                    log.info("========== 原始用户信息详情 ==========");
                    log.info("OpenId: {}", wxMpUser.getOpenId());
                    log.info("Nickname: {}", wxMpUser.getNickname());
                    log.info("HeadImgUrl: {}", wxMpUser.getHeadImgUrl());
                    log.info("Remark: {}", wxMpUser.getRemark());
                    log.info("UnionId: {}", wxMpUser.getUnionId());
                    log.info("Subscribe: {}", wxMpUser.getSubscribe());
                    log.info("SubscribeTime: {}", wxMpUser.getSubscribeTime());
                    log.info("Language: {}", wxMpUser.getLanguage());

                    // 打印完整的原始JSON数据
                    try {
                        log.info("完整用户对象JSON: {}", JsonUtils.toJson(wxMpUser));
                    } catch (Exception e) {
                        log.warn("无法序列化用户信息为JSON: {}", e.getMessage());
                    }
                    log.info("=====================================");

                    UserInfo userInfo = new UserInfo();
                    userInfo.setAvatarUrl(wxMpUser.getHeadImgUrl());
                    userInfo.setNickName(wxMpUser.getNickname());
                    userInfo.setRemark(StringUtils.isNotBlank(wxMpUser.getRemark()) ? wxMpUser.getRemark() : "");
                    userInfo.setWxAccount(StringUtils.isNotBlank(wxMpUser.getUnionId()) ? wxMpUser.getUnionId() : "");
                    userInfo.setWxId(wxMpUser.getOpenId());
                    userList.add(userInfo);
                }
            }

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> data = new HashMap<>();
            data.put("list", userList);
            data.put("total", userList.size());
            result.put("data", data);

            log.info("返回用户信息列表，共{}个用户", userList.size());

            return JsonUtils.toJson(result);
        } catch (WxErrorException e) {
            log.error("获取用户信息失败: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", e.getMessage());
            return JsonUtils.toJson(errorResult);
        } catch (Exception e) {
            log.error("获取用户信息异常: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", e.getMessage());
            return JsonUtils.toJson(errorResult);
        }
    }

    /**
     * <pre>
     * 获取用户管理列表
     * </pre>
     */
    @GetMapping("/list")
    public String getUserList(@PathVariable String appid,
                              @RequestParam(required = false) String nextOpenid) {
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

            List<UserInfo> userList = new ArrayList<>();
            // nextOpenid 为 null 时表示从第一个开始获取，这是正常的
            String currentNextOpenid = StringUtils.isBlank(nextOpenid) ? null : nextOpenid;

            log.info("开始获取用户列表，appid={}, nextOpenid={}", appid, currentNextOpenid);

            // 获取用户列表（支持分页）
            WxMpUserList wxMpUserList = wxMpService.getUserService().userList(currentNextOpenid);

            log.info("获取用户列表响应: total={}, count={}, nextOpenid={}",
                wxMpUserList != null ? wxMpUserList.getTotal() : null,
                wxMpUserList != null ? wxMpUserList.getCount() : null,
                wxMpUserList != null ? wxMpUserList.getNextOpenid() : null);

            if (wxMpUserList != null) {
                // 获取openid列表
                List<String> openIds = wxMpUserList.getOpenids();

                log.info("获取到的openid数量: {}", openIds != null ? openIds.size() : 0);

                if (openIds != null && !openIds.isEmpty()) {
                    // 批量获取用户信息（每次最多100个）
                    int batchSize = 100;

                    for (int i = 0; i < openIds.size(); i += batchSize) {
                        int end = Math.min(i + batchSize, openIds.size());
                        List<String> batchOpenIds = openIds.subList(i, end);

                        log.info("准备批量获取用户信息，批次索引: {}-{}, openid数量: {}", i, end - 1, batchOpenIds.size());
                        log.debug("批次openid列表: {}", JsonUtils.toJson(batchOpenIds));

                        // 批量获取用户信息
                        log.info("调用微信API批量获取用户信息，openid列表: {}", JsonUtils.toJson(batchOpenIds));
                        List<WxMpUser> wxMpUsers = wxMpService.getUserService().userInfoList(batchOpenIds);

                        log.info("批量获取用户信息响应，批次大小: {}, 实际返回用户数: {}", batchOpenIds.size(), wxMpUsers != null ? wxMpUsers.size() : 0);

                        // 打印整个批次响应的原始JSON
                        if (wxMpUsers != null && !wxMpUsers.isEmpty()) {
                            try {
                                log.info("批次用户信息完整JSON响应: {}", JsonUtils.toJson(wxMpUsers));
                            } catch (Exception e) {
                                log.warn("无法序列化批次用户信息为JSON: {}", e.getMessage());
                            }
                        }

                        for (WxMpUser wxMpUser : wxMpUsers) {
                            // 打印原始用户信息的关键字段
                            log.info("========== 原始用户信息详情 ==========");
                            log.info("OpenId: {}", wxMpUser.getOpenId());
                            log.info("Nickname: {}", wxMpUser.getNickname());
                            log.info("HeadImgUrl: {}", wxMpUser.getHeadImgUrl());
                            log.info("Remark: {}", wxMpUser.getRemark());
                            log.info("UnionId: {}", wxMpUser.getUnionId());
                            log.info("Subscribe: {}", wxMpUser.getSubscribe());
                            log.info("SubscribeTime: {}", wxMpUser.getSubscribeTime());
                            log.info("Language: {}", wxMpUser.getLanguage());

                            // 打印完整的原始JSON数据
                            try {
                                log.info("完整用户对象JSON: {}", JsonUtils.toJson(wxMpUser));
                            } catch (Exception e) {
                                log.warn("无法序列化用户信息为JSON: {}", e.getMessage());
                            }
                            log.info("=====================================");

                            UserInfo userInfo = new UserInfo();
                            userInfo.setAvatarUrl(wxMpUser.getHeadImgUrl());
                            userInfo.setNickName(wxMpUser.getNickname());
                            userInfo.setRemark(StringUtils.isNotBlank(wxMpUser.getRemark()) ? wxMpUser.getRemark() : "");
                            userInfo.setWxAccount(StringUtils.isNotBlank(wxMpUser.getUnionId()) ? wxMpUser.getUnionId() : "");
                            userInfo.setWxId(wxMpUser.getOpenId());
                            userList.add(userInfo);
                        }
                    }
                }
            }

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> data = new HashMap<>();
            data.put("list", userList);

            // 添加统计信息
            if (wxMpUserList != null) {
                data.put("total", wxMpUserList.getTotal());
                data.put("count", wxMpUserList.getCount());
                if (StringUtils.isNotBlank(wxMpUserList.getNextOpenid())) {
                    data.put("nextOpenid", wxMpUserList.getNextOpenid());
                }
            }

            result.put("data", data);

            log.info("返回用户列表，共{}个用户", userList.size());

            return JsonUtils.toJson(result);
        } catch (WxErrorException e) {
            log.error("获取用户列表失败: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", e.getMessage());
            return JsonUtils.toJson(errorResult);
        } catch (Exception e) {
            log.error("获取用户列表异常: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", e.getMessage());
            return JsonUtils.toJson(errorResult);
        }
    }

    /**
     * 用户信息实体类
     */
    @Data
    public static class UserInfo {
        @JsonProperty("avatar_url")
        private String avatarUrl;

        @JsonProperty("nick_name")
        private String nickName;

        private String remark;

        @JsonProperty("wx_account")
        private String wxAccount;

        @JsonProperty("wx_id")
        private String wxId;
    }

}
