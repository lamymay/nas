package com.arc.nas;

import com.arc.util.JSON;
import com.arc.util.StringTool;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.RestController;

/**
 * 启动类，项目将打包为jar，运行于内置tomcat
 *
 * @author may
 */
//EnableCaching  开启缓存
@EnableScheduling
@RestController
@EnableAsync
@EnableCaching
@EnableTransactionManagement
@MapperScan({"com.arc.nas.repository.mysql.mapper"})
@SpringBootApplication
        (exclude = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration.class

        })
@EnableAspectJAutoProxy
public class WebServerApplication {

    private static final Logger log = LoggerFactory.getLogger(WebServerApplication.class);


    public static void main(String[] args) {
        long t1 = System.currentTimeMillis();
        log.info("启动时参数打印{}", JSON.toJSONString(args));
        if (args != null) {
            for (String arg : args) {
                log.info("{}", arg);

            }
        }
        ConfigurableApplicationContext context = SpringApplication.run(WebServerApplication.class, args);
        log.info("项目启动耗时{}", StringTool.getTimeStringSoFar(t1));
        log.info("context.isActive={}", context.isActive());

    }


}
