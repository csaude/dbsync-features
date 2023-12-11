package org.mz.csaude.dbsyncfeatures.core.manager.datasource;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import liquibase.integration.spring.SpringLiquibase;

@Configuration
public class LiquibaseConfig {
   
    @Bean
    public SpringLiquibase liquibase(DataSource dataSource, Environment env) {

        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setContexts(StringUtils.join(env.getActiveProfiles(), ","));
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:/db/changelog/db.changelog-master.xml");
		liquibase.setDatabaseChangeLogTable("LIQUIBASECHANGELOG");
        liquibase.setDatabaseChangeLogLockTable("LIQUIBASECHANGELOGLOCK");
        liquibase.setShouldRun(true);
        return liquibase;
    }
}
