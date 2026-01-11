package com.arc.nas.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
//        String checkTableExists = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'SECURITY_USER'";
//        Integer count = jdbcTemplate.queryForObject(checkTableExists, Integer.class);
        try {
            log.info("Table init sql executing ...");
            ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
            resourceDatabasePopulator.addScript(new ClassPathResource("db/table/h2/init_table_h2.sql"));
            resourceDatabasePopulator.execute(dataSource);
            log.info("Table init sql execute successfully");

        } catch (Exception exception) {
            log.error("Table init sql execute error", exception);
        }

        try {
            log.info("insert data ...");
            //insertRows();

            log.info("Prepare insert data end!");

        } catch (Exception exception) {
            log.error("Table init sql execute error", exception);
        }
    }


}
