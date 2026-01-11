package com.arc.nas.config.db;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class JDBCTemplateConfig {

    @Bean(name = {"namedParameterJdbcOperations"})
    public NamedParameterJdbcOperations namedParameterJdbcOperations(@Qualifier("dataSource1") DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

}
