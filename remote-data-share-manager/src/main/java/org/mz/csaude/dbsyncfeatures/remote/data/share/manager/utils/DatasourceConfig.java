package org.mz.csaude.dbsyncfeatures.remote.data.share.manager.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DatasourceConfig {

    @Value("${spring.datasource.centralization.features.url}")
    private String dataBaseUrl;

    @Value("${spring.datasource.centralization.features.username}")
    private String username;

    @Value("${spring.datasource.centralization.features.password}")
    private String password;

    @Value("${spring.mngt-datasource.centralization.features.driver-class-name}")
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
