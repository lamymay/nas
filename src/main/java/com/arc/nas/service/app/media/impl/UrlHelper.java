package com.arc.nas.service.app.media.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    public String getPrefix() {
        if (prefix == null) {
            prefix = "PROTOCOLHOST:PORTCONTEXT_PATH/api/media/CODE"
                    .replace("PROTOCOL", getProtocol())
                    .replace("HOST", localNetAddress)
                    .replace("PORT", String.valueOf(port))
                    .replace("CONTEXT_PATH", contextPath);
        }
        return prefix;
    }


}
