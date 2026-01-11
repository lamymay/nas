package com.arc.nas.config.mybatis;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * Mybatis相关配置
 *
 * @author may
 * @since 2022.02.26
 */

@Configuration
//@MapperScan(basePackages = MybatisConfig.PACKAGE, sqlSessionFactoryRef = "sqlSessionFactory1")
public class MybatisConfig {

    // mapper 接口路径
    static final String mapperInterfacePackagePath = "com.arc.nas.repository.mysql.mapper";
    static final String mapperXMLConfigPath = "mapper/*/*.xml";
    private static final Logger log = LoggerFactory.getLogger(MybatisConfig.class);
    @Autowired
    @Qualifier("dataSource1")
    private DataSource dataSource1;

    @Bean(name = "transactionManager")
    public DataSourceTransactionManager masterTransactionManager() {
        return new DataSourceTransactionManager(dataSource1);
    }

    @Bean("sqlSessionFactory1")
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        MybatisSqlSessionFactoryBean sqlSessionFactory = new MybatisSqlSessionFactoryBean();
        sqlSessionFactory.setDataSource(dataSource1);

        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setJdbcTypeForNull(JdbcType.NULL);
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setCacheEnabled(false);
        sqlSessionFactory.setConfiguration(configuration);
        sqlSessionFactory.setTypeAliasesPackage(mapperInterfacePackagePath);

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        String xmlScanPath = PathMatchingResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + mapperXMLConfigPath;
        Resource[] resources = resolver.getResources(xmlScanPath);
        log.info("xml resources=\n{}", resources);

        sqlSessionFactory.setMapperLocations(resources);

        sqlSessionFactory.setTypeEnumsPackage("com.arc.core.enums.common.*");
        //PerformanceInterceptor(),OptimisticLockerInterceptor()
        //添加分页功能  sqlSessionFactory.setPlugins(new Interceptor[]{});
        return sqlSessionFactory.getObject();
    }


}
