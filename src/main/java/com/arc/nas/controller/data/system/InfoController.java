package com.arc.nas.controller.data.system;

import com.arc.nas.model.dto.ArcRuntimeEnvironment;
import com.arc.util.JSON;
import com.arc.util.StringTool;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
public class InfoController {

    private static final Logger log = LoggerFactory.getLogger(InfoController.class);

    @Value("${spring.profiles.active:unknown}")
    public String activeProfile;
    @Value("${server.port:80}")
    private int port;
    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @RequestMapping("/info")
    @ResponseBody
    public Object info(HttpServletRequest request, HttpServletResponse response) {
        log.info("################################");
        log.info("activeProfile={} ", activeProfile);
        log.info("port={} ", port);
        log.info("contextPath={} ", contextPath);
        log.info("request ServerName={} ", request.getServerName());
        log.info("request ServerName={} ", request.getRemoteAddr());

        String ipAddress = getIpAddress(request);
        log.info("测试 ipAddress={}", ipAddress);

        log.info("时间={},请求方法为 info ={},", LocalDateTime.now(), request);
        ArcRuntimeEnvironment arcRuntimeEnvironment = ArcRuntimeEnvironment.getArcRuntimeEnvironment();

        log.info("################################");

        Map<String, String> requestParamMap = new HashMap<>(16);
        Enumeration<?> temp = request.getParameterNames();
        if (null != temp) {
            while (temp.hasMoreElements()) {
                String en = (String) temp.nextElement();
                String value = request.getParameter(en);
                requestParamMap.put(en, value);
            }
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("info", request.getContextPath() + "  " + request.getRequestURI() + "  " + request.getRemoteHost() + "  ");
        map.put("ArcRuntimeEnvironment", arcRuntimeEnvironment);
        map.put("requestParamMap", requestParamMap);
        map.put("now", System.currentTimeMillis());
        response.setHeader("server_response_time", "" + System.currentTimeMillis());
        response.setHeader("doc/test", "true");
        return map;
    }

    @RequestMapping("/info/2")
    @ResponseBody
    public Object infoV2(HttpServletRequest request, HttpServletResponse response) throws InterruptedException {
        log.info("时间={},请求方法为 info ={},", LocalDateTime.now(), request);
        log.info("request ServerName={} ", request.getServerName());
        log.info("request ServerName={} ", request.getRemoteAddr());

        Thread.sleep(500);
        String ipAddress = getIpAddress(request);
        log.info("测试 ipAddress={}", ipAddress);
        Map<String, String> requestParamMap = new HashMap<>(16);
        Enumeration<?> temp = request.getParameterNames();
        if (null != temp) {
            while (temp.hasMoreElements()) {
                String en = (String) temp.nextElement();
                String value = request.getParameter(en);
                requestParamMap.put(en, value);
            }
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("now", System.currentTimeMillis());
        map.put("getContextPath", request.getContextPath());
        map.put("getRequestURI", request.getRequestURI());
        map.put("getRemoteHost", request.getRemoteHost());
        map.put("ipAddress", ipAddress);
        map.put("requestParamMap", requestParamMap);
        response.setHeader("doc/test", "" + System.currentTimeMillis());
        return map;
    }

    public static String getIpAddress(HttpServletRequest request) {
        try {
            String ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
                if (ipAddress.equals("127.0.0.1")) {
                    // 根据网卡取本机配置的IP
                    ipAddress = InetAddress.getLocalHost().getHostAddress();
                }
            }
            // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
            if (ipAddress != null && ipAddress.length() > 15) { // "***.***.***.***".length()
                // = 15
                if (ipAddress.indexOf(",") > 0) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
            }
            return (ipAddress != null && !"".equals(ipAddress.trim())) ? ipAddress : "unknown";


        } catch (Exception e) {
            return "unknown";
        }
    }



    @ResponseBody
    @GetMapping("/echo")
    public Object echo(@RequestParam String message) {
        log.info("echo message={}", message);
        return message + System.currentTimeMillis();
    }

    @ResponseBody
    @PostMapping("/echo/map")
    public Map<String, Object> echoMap(@RequestBody Map<String, Object> map) {
        long t1 = System.currentTimeMillis();
        log.info("###echo message={}", JSON.toJSONString(map));
        String cost = StringTool.getTimeStringSoFar(t1);
        log.info("###log cost ={}", cost);
        map.put("cost", cost);
        map.put("serverTime", System.currentTimeMillis());
        return map;
    }

}
