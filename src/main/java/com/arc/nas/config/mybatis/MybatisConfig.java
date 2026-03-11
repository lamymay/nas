package com.arc.nas.config.mybatis;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * Mybatis相关配置 - 纯 JavaConfig 方式（不使用注解）
 */
@Configuration
public class MybatisConfig {
    private static final Logger log = LoggerFactory.getLogger(MybatisConfig.class);

    static final String MODEL_PACKAGE = "com.arc.nas.model.domain.system.common";
    static final String MAPPER_INTERFACE_PACKAGE = "com.arc.nas.repository.mapper.system";
    static final String MAPPER_XML_PATH = "classpath*:/mapper/*/*.xml";

    @Autowired
    @Qualifier("dataSource1")
    private DataSource dataSource1;

    /**
     * 手动配置 Mapper 扫描器
     * 这是替代 @MapperScan 的核心逻辑
     * 将此方法改为 static
     * 这样它就不会依赖于当前类的实例，避免了“创建太早”导致的增强失败
     */
    @Bean
    public static MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer scannerConfigurer = new MapperScannerConfigurer();
        // 1. 指定要扫描的接口包路径
        scannerConfigurer.setBasePackage(MAPPER_INTERFACE_PACKAGE);
        // 2. 关键：明确关联到你的 SqlSessionFactory Bean 名称
        scannerConfigurer.setSqlSessionFactoryBeanName("factoryBean1");
        return scannerConfigurer;
    }


    @Bean(name = "transactionManager")
    public DataSourceTransactionManager masterTransactionManager() {
        return new DataSourceTransactionManager(dataSource1);
    }

    @Bean("factoryBean1")
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource1);

        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setJdbcTypeForNull(JdbcType.NULL);
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setCacheEnabled(false);
        factoryBean.setConfiguration(configuration);

        // 修复点：合并别名路径，防止覆盖
        factoryBean.setTypeAliasesPackage(MODEL_PACKAGE + "," + MAPPER_INTERFACE_PACKAGE);

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        factoryBean.setMapperLocations(resolver.getResources(MAPPER_XML_PATH));

        // 枚举包扫描
        factoryBean.setTypeEnumsPackage("com.arc.nas.model.enums");

        return factoryBean.getObject();
    }

}