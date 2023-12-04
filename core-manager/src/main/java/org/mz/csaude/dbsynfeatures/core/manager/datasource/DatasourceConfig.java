package org.mz.csaude.dbsynfeatures.core.manager.datasource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DatasourceConfig {

    @Value("${spring.datasource.notifications.url}")
    private String dataBaseUrl;

    @Value("${spring.datasource.notifications.username}")
    private String username;

    @Value("${spring.datasource.notifications.password}")
    private String password;

    @Value("${spring.mngt-datasource.notifications.driver-class-name}")
    private String mysqlDriver;


    @Bean
    public DataSource dataSource(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(dataBaseUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(mysqlDriver);

        return dataSource;
    }
}
