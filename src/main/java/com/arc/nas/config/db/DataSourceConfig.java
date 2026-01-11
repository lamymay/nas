package com.arc.nas.config.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 数据源配置
 *
 */
@Configuration
public class DataSourceConfig {
    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${spring.datasource.db1.url}")
    public String datasourceUrl1;

    @Value("${spring.datasource.db1.username}")
    public String datasourceUsername1;

    @Value("${spring.datasource.db1.password}")
    public String datasourcePassword1;

    @Value("${spring.datasource.db1.driver-class-name:'com.mysql.cj.jdbc.Driver'}")
    public String datasourceDriverClassName1;

    @Bean(name = "dataSource1")
    public DataSource dataSource1() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(datasourceDriverClassName1);
        hikariConfig.setJdbcUrl(datasourceUrl1);
        hikariConfig.setUsername(datasourceUsername1);
        hikariConfig.setPassword(datasourcePassword1);
        log.info("\n##########################################################################################" +
                        "\n配置Hikari数据源1:" +
                        "\ndatasourceJdbcUrl1={}" +
                        "\ndatasourceUsername1={}" +
                        "\ndatasourcePassword1={}" +
                        "\ndatasourceDriverClassName1={}" +
                        "\n##########################################################################################",
                hikariConfig.getJdbcUrl(), hikariConfig.getUsername(),
                datasourcePassword1, hikariConfig.getDriverClassName());
        DataSource hikariDataSource = new HikariDataSource(hikariConfig);
        log.info("数据源1 DataSource={}", hikariDataSource);
        return hikariDataSource;

    }

}
