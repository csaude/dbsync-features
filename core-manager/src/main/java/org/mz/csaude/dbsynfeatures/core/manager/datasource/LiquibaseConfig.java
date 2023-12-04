package org.mz.csaude.dbsynfeatures.core.manager.datasource;

import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration
public class LiquibaseConfig {

    @Value("${spring.liquibase.notifications.change-log}")
    private String changeLogPath;

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource, Environment env) {

        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setContexts(StringUtils.join(env.getActiveProfiles(), ","));
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(changeLogPath);
        liquibase.setDatabaseChangeLogTable("LIQUIBASECHANGELOG");
        liquibase.setDatabaseChangeLogLockTable("LIQUIBASECHANGELOGLOCK");
        liquibase.setShouldRun(true);
        return liquibase;
    }
}
