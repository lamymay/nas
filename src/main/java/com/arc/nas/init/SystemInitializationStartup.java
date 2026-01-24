package com.arc.nas.init;

import com.arc.nas.model.dto.ArcRuntimeEnvironment;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;


/**
 * 初始化操作
 */
@Component
public class SystemInitializationStartup implements ApplicationListener<ContextRefreshedEvent> {
    final static String HTTP_PROTOCOL = "http://";
    final static String HTTPS_PROTOCOL = "https://";
    final static String serverIp = "127.0.0.1";
    private static final Logger log = LoggerFactory.getLogger(SystemInitializationStartup.class);
    /**
     * web.system.initial:是否在系统启动的时候初始化一些操作
     * 注意：1、建议配置在配置文件中 2、缺省为true
     */
    @Value("${arc.system.initial:true}")
    public boolean initial;
    @Value("${spring.profiles.active:''}")
    public String activeProfile;
    /**
     *
     */
    @Value("${server.servlet.context-path:/}")
    public String servletContextPath;
    /**
     * 端口
     */
    @Value("${server.port:8000}")
    public int port;
    /**
     * SSL证书相关
     */
    @Value("${server.ssl.key-store:''}")
    public String serverSslKeyStore;

    @Autowired
    private Environment env;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        System.out.println("容器刷新事件 SystemInitializationStartup");
        //Spring容器加载完毕之后执行: 以下方法
        log.info("系统启动后触发初始化={}", initial);
        if (initial) {
            new Runnable() {
                @Override
                public void run() {
                    printFileServerPath();
                }
            }.run();
        }
//        String[] activeProfiles = env.getActiveProfiles();
//        Object requiredProperty = env.getRequiredProperty("propertySources");
        // 初始化文件上传的输出文件夹
        ReadyResourceInit.init();

        System.out.println(ReadyResourceInit.getWriteableDirectory());


    }

    public static void main(String[] args) {
        //print();
        System.out.println(new SystemInitializationStartup().getUrlStartWith(null));
        System.out.println(new SystemInitializationStartup().getUrlStartWith(""));
        System.out.println(new SystemInitializationStartup().getUrlStartWith(" "));
        System.out.println(new SystemInitializationStartup().getUrlStartWith("abc"));
        System.out.println(new SystemInitializationStartup().getUrlStartWith("/abc/"));
        System.out.println(new SystemInitializationStartup().getUrlStartWith("/abc"));
        System.out.println(new SystemInitializationStartup().getUrlStartWith("abc/"));

    }

    public String getProtocol() {
        if (StringUtils.isBlank(serverSslKeyStore)) {
            return HTTP_PROTOCOL;
        }
        return HTTPS_PROTOCOL;

    }

    public String getActiveProfile() {
        return activeProfile;
    }

    public void setActiveProfile(String activeProfile) {
        this.activeProfile = activeProfile;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }


    public void printFileServerPath() {
        System.out.println("##################################################");
        System.out.println(" Spring容器加载完毕之后执行的方法可以做一些扩展  ");
        ArcRuntimeEnvironment environment = ArcRuntimeEnvironment.getArcRuntimeEnvironment();
        //获取此类中的所有字段
        Class clz = environment.getClass();

        Field[] fields = clz.getDeclaredFields();
        //获取字段的名称

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            try {
                //f.getName()得到对应字段的属性名，f.get(o)得到对应字段属性值,f.getGenericType()得到对应字段的类型
                String message = " 类型:" + field.getGenericType() + " 属性是:" + field.getName() + "=" + field.get(environment);
                System.out.println(message);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }


        log.info("spring.profiles.active ={}", activeProfile);
        log.info("spring.application.name ={}", env.getProperty("spring.application.name"));
        System.out.println(getProtocol() + serverIp + ":" + port + getServletContextPath() + "/info");
        System.out.println(getProtocol() + serverIp + ":" + port + getServletContextPath() + "/doc/test/file");
        System.out.println(getProtocol() + serverIp + ":" + port + getServletContextPath() + "/v1/file/id");
        System.out.println("##################################################");
    }

    private String getServletContextPath() {

        return getUrlStartWith(this.servletContextPath);

    }

    public void setServletContextPath(String servletContextPath) {
        this.servletContextPath = servletContextPath;
    }

    private String getUrlStartWith(String servletContextPath) {
        if (servletContextPath == null) {
            servletContextPath = "";
        }
        if (servletContextPath.endsWith("/")) {
            servletContextPath = servletContextPath.substring(0, servletContextPath.lastIndexOf("/"));
        }

        if (!servletContextPath.startsWith("/")) {
            servletContextPath = "/" + servletContextPath;
        }

        return servletContextPath;

    }


}
//系统会存在两个容器，一个是root application context ,另一个就是我们自己的 projectName-servlet context（作为root application context的子容器）
// 这种情况下，就会造成onApplicationEvent方法被执行两次。为了避免上面提到的问题，我们可以只在root application context初始化完成后调用逻辑代码，其他的容器的初始化完成，则不做任何处理。
// event.getApplicationContext().getParent() == null
