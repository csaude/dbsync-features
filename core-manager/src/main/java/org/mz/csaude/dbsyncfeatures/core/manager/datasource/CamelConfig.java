package org.mz.csaude.dbsyncfeatures.core.manager.datasource;

import org.apache.camel.component.jpa.JpaComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/***
 * Camel configuration class to use JPA endpoint
 */
@Configuration
public class CamelConfig {

    @Bean
    public JpaComponent jpa(EntityManagerFactory entityManagerFactory, DataSource dataSource) {
        JpaComponent jpaComponent = new JpaComponent();
        jpaComponent.setEntityManagerFactory(entityManagerFactory);
        return jpaComponent;
    }
}
