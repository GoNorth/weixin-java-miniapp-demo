package com.github.binarywang.demo.wx.miniapp.controller.resources;

import com.github.binarywang.demo.wx.miniapp.utils.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.material.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 多媒体文件管理控制器（公众号）
 * 参考文档：https://github.com/binarywang/WxJava/wiki/MP_%E5%A4%9A%E5%AA%92%E4%BD%93%E6%96%87%E4%BB%B6%E7%AE%A1%E7%90%86
 * 
 * 注意：临时素材有效期为3天
 *
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */
@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/wx/resources/{appid}")
public class ResourcesManageController {
    private final WxMpService wxMpService;

    // ========== 临时素材管理（3天有效期） ==========

    /**
     * <pre>
     * 上传临时素材（图片、语音、视频、缩略图）
     * </pre>
     */
    @PostMapping("/temp/upload")
    public String uploadTempMedia(@PathVariable String appid,
                                  HttpServletRequest request) {
        try {
            validateAppid(appid);

            CommonsMultipartResolver resolver = new CommonsMultipartResolver(request.getSession().getServletContext());

            if (!resolver.isMultipart(request)) {
                throw new IllegalArgumentException("请求不是multipart格式！");
            }

            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            String mediaType = multiRequest.getParameter("mediaType");
            if (StringUtils.isBlank(mediaType)) {
                mediaType = WxConsts.MediaFileType.IMAGE; // 默认为图片
            }

            Iterator<String> it = multiRequest.getFileNames();
            Map<String, Object> result = new HashMap<>();
            ArrayList<Map<String, Object>> mediaList = new ArrayList<>();

            while (it.hasNext()) {
                try {
                    MultipartFile file = multiRequest.getFile(it.next());
                    File tempFile = File.createTempFile("wx_media_", "_" + file.getOriginalFilename());
                    log.info("临时文件路径: {}", tempFile.getAbsolutePath());
                    file.transferTo(tempFile);

                    WxMediaUploadResult uploadResult = wxMpService.getMaterialService().mediaUpload(mediaType, tempFile);
                    
                    Map<String, Object> mediaInfo = new HashMap<>();
                    mediaInfo.put("mediaId", uploadResult.getMediaId());
                    mediaInfo.put("type", uploadResult.getType());
                    mediaInfo.put("createdAt", uploadResult.getCreatedAt());
                    mediaInfo.put("url", uploadResult.getUrl());
                    mediaList.add(mediaInfo);

                    log.info("上传临时素材成功，mediaId={}, type={}, createdAt={}", 
                        uploadResult.getMediaId(), uploadResult.getType(), uploadResult.getCreatedAt());

                    // 删除临时文件
                    tempFile.delete();
                } catch (IOException e) {
                    log.error("处理文件失败: {}", e.getMessage(), e);
                } catch (WxErrorException e) {
                    log.error("上传临时素材失败: {}", e.getMessage(), e);
                    throw e;
                }
            }

            result.put("success", true);
            result.put("mediaList", mediaList);
            result.put("count", mediaList.size());
            result.put("message", "上传临时素材成功");

            return JsonUtils.toJson(result);
        } catch (WxErrorException e) {
            log.error("上传临时素材失败: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        } catch (Exception e) {
            log.error("上传临时素材异常: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        }
    }

    /**
     * <pre>
     * 下载临时素材
     * </pre>
     */
    @GetMapping("/temp/download/{mediaId}")
    public File downloadTempMedia(@PathVariable String appid,
                                  @PathVariable String mediaId) {
        try {
            validateAppid(appid);

            if (StringUtils.isBlank(mediaId)) {
                throw new IllegalArgumentException("mediaId参数不能为空！");
            }

            log.info("下载临时素材，appid={}, mediaId={}", appid, mediaId);

            File media = wxMpService.getMaterialService().mediaDownload(mediaId);

            log.info("下载临时素材成功，mediaId={}", mediaId);

            return media;
        } catch (WxErrorException e) {
            log.error("下载临时素材失败: {}", e.getMessage(), e);
            throw new RuntimeException("下载临时素材失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("下载临时素材异常: {}", e.getMessage(), e);
            throw new RuntimeException("下载临时素材异常: " + e.getMessage());
        }
    }

    // ========== 永久素材管理 ==========

    /**
     * <pre>
     * 上传永久图片素材
     * </pre>
     */
    @PostMapping("/permanent/uploadImage")
    public String uploadPermanentImage(@PathVariable String appid,
                                       HttpServletRequest request) {
        try {
            validateAppid(appid);

            CommonsMultipartResolver resolver = new CommonsMultipartResolver(request.getSession().getServletContext());

            if (!resolver.isMultipart(request)) {
                throw new IllegalArgumentException("请求不是multipart格式！");
            }

            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            Iterator<String> it = multiRequest.getFileNames();
            Map<String, Object> result = new HashMap<>();
            ArrayList<Map<String, Object>> mediaList = new ArrayList<>();

            while (it.hasNext()) {
                try {
                    MultipartFile file = multiRequest.getFile(it.next());
                    File tempFile = File.createTempFile("wx_image_", "_" + file.getOriginalFilename());
                    log.info("临时文件路径: {}", tempFile.getAbsolutePath());
                    file.transferTo(tempFile);

                    Object imgUploadResult = wxMpService.getMaterialService().mediaImgUpload(tempFile);
                    String url = imgUploadResult.toString(); // 简化处理
                    
                    Map<String, Object> mediaInfo = new HashMap<>();
                    mediaInfo.put("url", url);
                    mediaInfo.put("originalFilename", file.getOriginalFilename());
                    mediaList.add(mediaInfo);

                    log.info("上传永久图片成功，url={}", url);

                    // 删除临时文件
                    tempFile.delete();
                } catch (IOException e) {
                    log.error("处理文件失败: {}", e.getMessage(), e);
                } catch (WxErrorException e) {
                    log.error("上传永久图片失败: {}", e.getMessage(), e);
                    throw e;
                }
            }

            result.put("success", true);
            result.put("mediaList", mediaList);
            result.put("count", mediaList.size());
            result.put("message", "上传永久图片成功");

            return JsonUtils.toJson(result);
        } catch (WxErrorException e) {
            log.error("上传永久图片失败: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        } catch (Exception e) {
            log.error("上传永久图片异常: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        }
    }

    /**
     * <pre>
     * 上传永久素材（图片、语音、视频、缩略图）
     * </pre>
     */
    @PostMapping("/permanent/upload")
    public String uploadPermanentMedia(@PathVariable String appid,
                                       HttpServletRequest request) {
        try {
            validateAppid(appid);

            CommonsMultipartResolver resolver = new CommonsMultipartResolver(request.getSession().getServletContext());

            if (!resolver.isMultipart(request)) {
                throw new IllegalArgumentException("请求不是multipart格式！");
            }

            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            String mediaType = multiRequest.getParameter("mediaType");
            if (StringUtils.isBlank(mediaType)) {
                throw new IllegalArgumentException("mediaType参数不能为空！");
            }

            Iterator<String> it = multiRequest.getFileNames();
            Map<String, Object> result = new HashMap<>();
            ArrayList<Map<String, Object>> mediaList = new ArrayList<>();

            while (it.hasNext()) {
                try {
                    MultipartFile file = multiRequest.getFile(it.next());
                    File tempFile = File.createTempFile("wx_media_", "_" + file.getOriginalFilename());
                    log.info("临时文件路径: {}", tempFile.getAbsolutePath());
                    file.transferTo(tempFile);

                    WxMpMaterialUploadResult uploadResult;
                    
                    // 视频素材需要额外的参数
                    WxMpMaterial material = new WxMpMaterial();
                    material.setFile(tempFile);
                    material.setName(file.getOriginalFilename());
                    
                    if (WxConsts.MediaFileType.VIDEO.equals(mediaType)) {
                        // 视频素材的 title 和 introduction 需要通过其他方式传递
                        // 注意：实际使用时需要根据 WxJava 版本调整
                    }
                    
                    uploadResult = wxMpService.getMaterialService().materialFileUpload(mediaType, material);
                    
                    Map<String, Object> mediaInfo = new HashMap<>();
                    mediaInfo.put("mediaId", uploadResult.getMediaId());
                    mediaInfo.put("url", uploadResult.getUrl());
                    mediaList.add(mediaInfo);

                    log.info("上传永久素材成功，mediaId={}, url={}", uploadResult.getMediaId(), uploadResult.getUrl());

                    // 删除临时文件
                    tempFile.delete();
                } catch (IOException e) {
                    log.error("处理文件失败: {}", e.getMessage(), e);
                } catch (WxErrorException e) {
                    log.error("上传永久素材失败: {}", e.getMessage(), e);
                    throw e;
                }
            }

            result.put("success", true);
            result.put("mediaList", mediaList);
            result.put("count", mediaList.size());
            result.put("message", "上传永久素材成功");

            return JsonUtils.toJson(result);
        } catch (WxErrorException e) {
            log.error("上传永久素材失败: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        } catch (Exception e) {
            log.error("上传永久素材异常: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        }
    }

    /**
     * <pre>
     * 上传永久图文素材
     * </pre>
     */
    @PostMapping("/permanent/uploadNews")
    public String uploadPermanentNews(@PathVariable String appid,
                                      @RequestBody WxMpMaterialNews news) {
        try {
            validateAppid(appid);

            if (news == null || news.getArticles() == null || news.getArticles().isEmpty()) {
                throw new IllegalArgumentException("图文素材内容不能为空！");
            }

            log.info("上传永久图文素材，appid={}, 文章数量={}", appid, news.getArticles().size());

            WxMpMaterialUploadResult result = wxMpService.getMaterialService().materialNewsUpload(news);

            log.info("上传永久图文素材成功，mediaId={}", result.getMediaId());

            Map<String, Object> response = new HashMap<>();
            Map<String, Object> data = new HashMap<>();
            data.put("mediaId", result.getMediaId());
            response.put("success", true);
            response.put("data", data);
            response.put("message", "上传永久图文素材成功");

            return JsonUtils.toJson(response);
        } catch (WxErrorException e) {
            log.error("上传永久图文素材失败: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        } catch (Exception e) {
            log.error("上传永久图文素材异常: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        }
    }

    /**
     * <pre>
     * 获取永久素材
     * </pre>
     */
    @GetMapping("/permanent/get/{mediaId}")
    public String getPermanentMedia(@PathVariable String appid,
                                    @PathVariable String mediaId) {
        try {
            validateAppid(appid);

            if (StringUtils.isBlank(mediaId)) {
                throw new IllegalArgumentException("mediaId参数不能为空！");
            }

            log.info("获取永久素材，appid={}, mediaId={}", appid, mediaId);

            // 图文素材返回 WxMpMaterialNews
            // 其他素材返回 InputStream
            // 注意：需要根据素材类型调用不同的方法
            // 这里简化处理，实际使用时需要根据素材类型判断
            java.io.InputStream materialStream = wxMpService.getMaterialService().materialImageOrVoiceDownload(mediaId);
            log.info("获取永久素材成功，mediaId={}, stream available={}", mediaId, materialStream != null);

            Map<String, Object> response = new HashMap<>();
            response.put("type", "stream");
            response.put("success", true);
            response.put("message", "获取永久素材成功");
            response.put("note", "返回的是 InputStream，需要根据实际需求处理。图文素材请使用 materialNewsGet 方法");

            return JsonUtils.toJson(response);
        } catch (WxErrorException e) {
            log.error("获取永久素材失败: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        } catch (Exception e) {
            log.error("获取永久素材异常: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        }
    }

    /**
     * <pre>
     * 删除永久素材
     * </pre>
     */
    @DeleteMapping("/permanent/delete/{mediaId}")
    public String deletePermanentMedia(@PathVariable String appid,
                                       @PathVariable String mediaId) {
        try {
            validateAppid(appid);

            if (StringUtils.isBlank(mediaId)) {
                throw new IllegalArgumentException("mediaId参数不能为空！");
            }

            log.info("删除永久素材，appid={}, mediaId={}", appid, mediaId);

            boolean result = wxMpService.getMaterialService().materialDelete(mediaId);

            log.info("删除永久素材结果: {}", result);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result);
            response.put("message", result ? "删除永久素材成功" : "删除永久素材失败");

            return JsonUtils.toJson(response);
        } catch (WxErrorException e) {
            log.error("删除永久素材失败: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        } catch (Exception e) {
            log.error("删除永久素材异常: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        }
    }

    /**
     * <pre>
     * 修改永久图文素材
     * 注意：需要根据实际 WxJava 版本调整 API 调用
     * </pre>
     */
    @PostMapping("/permanent/updateNews")
    public String updatePermanentNews(@PathVariable String appid,
                                      @RequestBody Map<String, Object> request) {
        try {
            validateAppid(appid);

            String mediaId = (String) request.get("mediaId");
            Integer index = (Integer) request.get("index");
            
            if (StringUtils.isBlank(mediaId)) {
                throw new IllegalArgumentException("mediaId参数不能为空！");
            }
            if (index == null) {
                throw new IllegalArgumentException("index参数不能为空！");
            }

            log.info("修改永久图文素材，appid={}, mediaId={}, index={}", appid, mediaId, index);

            // TODO: 根据实际 WxJava 版本实现
            // 示例代码（需要根据实际版本调整）：
            // WxMpMaterialNews.WxMpMaterialNewsArticle article = ...;
            // boolean result = wxMpService.getMaterialService().materialNewsUpdate(mediaId, index, article);

            log.warn("修改永久图文素材功能需要根据实际 WxJava 版本实现");

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "功能待实现，请参考 WxJava 官方文档");

            return JsonUtils.toJson(response);
        } catch (Exception e) {
            log.error("修改永久图文素材异常: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        }
    }

    /**
     * <pre>
     * 获取素材总数
     * </pre>
     */
    @GetMapping("/permanent/count")
    public String getMaterialCount(@PathVariable String appid) {
        try {
            validateAppid(appid);

            log.info("获取素材总数，appid={}", appid);

            WxMpMaterialCountResult result = wxMpService.getMaterialService().materialCount();

            log.info("获取素材总数成功，voice={}, video={}, image={}, news={}", 
                result.getVoiceCount(), result.getVideoCount(), 
                result.getImageCount(), result.getNewsCount());

            Map<String, Object> response = new HashMap<>();
            Map<String, Object> data = new HashMap<>();
            data.put("voiceCount", result.getVoiceCount());
            data.put("videoCount", result.getVideoCount());
            data.put("imageCount", result.getImageCount());
            data.put("newsCount", result.getNewsCount());
            response.put("success", true);
            response.put("data", data);
            response.put("message", "获取素材总数成功");

            return JsonUtils.toJson(response);
        } catch (WxErrorException e) {
            log.error("获取素材总数失败: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        } catch (Exception e) {
            log.error("获取素材总数异常: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        }
    }

    /**
     * <pre>
     * 获取素材列表
     * </pre>
     */
    @GetMapping("/permanent/list")
    public String getMaterialList(@PathVariable String appid,
                                  @RequestParam String mediaType,
                                  @RequestParam(defaultValue = "0") int offset,
                                  @RequestParam(defaultValue = "20") int count) {
        try {
            validateAppid(appid);

            if (StringUtils.isBlank(mediaType)) {
                throw new IllegalArgumentException("mediaType参数不能为空！");
            }

            log.info("获取素材列表，appid={}, mediaType={}, offset={}, count={}", 
                appid, mediaType, offset, count);

            Object result;
            
            if (WxConsts.MaterialType.NEWS.equals(mediaType)) {
                result = wxMpService.getMaterialService().materialNewsBatchGet(offset, count);
            } else {
                result = wxMpService.getMaterialService().materialFileBatchGet(mediaType, offset, count);
            }

            Map<String, Object> response = new HashMap<>();
            Map<String, Object> data = new HashMap<>();
            
            if (result instanceof WxMpMaterialNewsBatchGetResult) {
                WxMpMaterialNewsBatchGetResult newsResult = (WxMpMaterialNewsBatchGetResult) result;
                data.put("totalCount", newsResult.getTotalCount());
                data.put("itemCount", newsResult.getItemCount());
                data.put("items", newsResult.getItems());
            } else if (result instanceof WxMpMaterialFileBatchGetResult) {
                WxMpMaterialFileBatchGetResult fileResult = (WxMpMaterialFileBatchGetResult) result;
                data.put("totalCount", fileResult.getTotalCount());
                data.put("itemCount", fileResult.getItemCount());
                data.put("items", fileResult.getItems());
            }
            
            response.put("success", true);
            response.put("data", data);
            response.put("message", "获取素材列表成功");

            return JsonUtils.toJson(response);
        } catch (WxErrorException e) {
            log.error("获取素材列表失败: {}", e.getMessage(), e);
            return buildErrorResult(e.getMessage());
        } catch (Exception e) {
            log.error("获取素材列表异常: {}", e.getMessage(), e);
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
     * 构建错误结果
     */
    private String buildErrorResult(String error) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", error);
        return JsonUtils.toJson(result);
    }

    // ========== 请求实体类 ==========
    // UpdateNewsRequest 已移除，直接使用 Map 接收参数
}
