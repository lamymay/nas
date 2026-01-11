package com.arc.nas.service.app.media.impl;

import cn.hutool.core.bean.BeanUtil;
import com.arc.nas.model.domain.system.common.SysFile;
import com.arc.nas.model.dto.app.media.MediaItemDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.arc.nas.init.GetLocalIPAddress.localNetAddress;

@Service
public class UrlHelper {

    final static String HTTP_PROTOCOL = "http://";
    final static String HTTPS_PROTOCOL = "https://";
    @Value("${server.port:8000}")
    public int port;
    /**
     * SSL证书相关
     */
    @Value("${server.ssl.key-store:''}")
    public String serverSslKeyStore;
    @Value("${server.servlet.context-path:/}")
    private String contextPath;
    private String prefix;

    public String getProtocol() {
        if (StringUtils.isBlank(serverSslKeyStore)) {
            return HTTP_PROTOCOL;
        }
        return HTTPS_PROTOCOL;

    }

    private String getPrefix() {
        if (prefix == null) {
            prefix = "PROTOCOLHOST:PORTCONTEXT_PATH/api/media/CODE"
                    .replace("PROTOCOL", getProtocol())
                    .replace("HOST", localNetAddress)
                    .replace("PORT", String.valueOf(port))
                    .replace("CONTEXT_PATH", contextPath);
        }
        return prefix;
    }

    public List<MediaItemDTO> covertSysFileToMediaItemDTO(List<SysFile> contents) {
        return contents.stream().map(sysFile -> {
            MediaItemDTO dto = new MediaItemDTO();
            BeanUtil.copyProperties(sysFile, dto);
            // 构建访问 URL
            dto.setUrl(buildFileUrl(sysFile.getCode()));
            dto.setThumbnails(buildThumbnails(sysFile.getThumbnail()));
            return dto;
        }).toList();

    }

    private Set<String> buildThumbnails(String thumbnail) {
        HashSet<String> thumbnails = new HashSet<>();
        if (thumbnail != null) {
            for (String thumbnailCode : thumbnail.split(",")) {
                thumbnails.add(buildFileUrl(thumbnailCode));
            }
        }
        return thumbnails;
    }

    // 构建文件访问 URL
    private String buildFileUrl(String code) {
        // 如果存储在 LOCAL，可以用 contextPath + uri
        // 如果 OSS/NAS 可以构建完整 URL
        return getPrefix().replace("CODE", code);
    }


}
