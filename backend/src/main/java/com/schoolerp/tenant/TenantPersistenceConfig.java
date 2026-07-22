package com.schoolerp.tenant;

import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * The TENANT persistence unit backs every existing entity/repository in the app
 * (users, students, staff, classes, attendance, fees, school_settings, ...). It is marked
 * @Primary so that all pre-existing repositories - which don't reference any qualifier -
 * keep working unmodified; they now transparently talk to whichever school's database
 * TenantContext points at for the current request.
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "com.schoolerp.repository",
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
public class TenantPersistenceConfig {

    @Bean
    @Primary
    public DataSource dataSource(TenantDataSourceProvider provider) {
        return new TenantRoutingDataSource(provider);
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder, DataSource dataSource) {

        Map<String, Object> jpaProperties = new HashMap<>();
        // "update" runs at EMF bootstrap time only, against whichever tenant happens to be
        // current at that moment (usually none). Schema for NEW schools is instead created
        // explicitly by TenantSchemaInitializer when a school is registered - see
        // com.schoolerp.master.service.SchoolService.
        jpaProperties.put("hibernate.hbm2ddl.auto", "none");
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
		jpaProperties.put("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");


        return builder
                .dataSource(dataSource)
                .packages("com.schoolerp.entity")
                .persistenceUnit("tenant")
                .properties(jpaProperties)
                .build();
    }

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean emf) {
        return new JpaTransactionManager(emf.getObject());
    }
}
