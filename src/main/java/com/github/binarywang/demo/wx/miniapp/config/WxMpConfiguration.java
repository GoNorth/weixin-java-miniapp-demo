package com.github.binarywang.demo.wx.miniapp.config;

import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 微信公众号配置
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */
@Configuration
@EnableConfigurationProperties(WxMpProperties.class)
public class WxMpConfiguration {
    private final WxMpProperties properties;

    @Autowired
    public WxMpConfiguration(WxMpProperties properties) {
        this.properties = properties;
    }

    @Bean
    public WxMpService wxMpService() {
        List<WxMpProperties.Config> configs = this.properties.getConfigs();
        // 允许配置为空，但会在运行时检查
        WxMpService mpService = new WxMpServiceImpl();
        if (configs != null && !configs.isEmpty()) {
            mpService.setMultiConfigStorages(
                configs.stream()
                    .filter(a -> a.getAppId() != null && !a.getAppId().trim().isEmpty())
                    .map(a -> {
                        WxMpDefaultConfigImpl config = new WxMpDefaultConfigImpl();
                        config.setAppId(a.getAppId());
                        config.setSecret(a.getSecret());
                        config.setToken(a.getToken());
                        config.setAesKey(a.getAesKey());
                        return config;
                    }).collect(Collectors.toMap(WxMpDefaultConfigImpl::getAppId, a -> a, (o, n) -> o)));
        }
        return mpService;
    }
}

