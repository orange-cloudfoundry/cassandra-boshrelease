package com.orange.oss.osb.cassandra.cassandraunit;

import com.datastax.driver.core.PlainTextAuthProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.data.cassandra.config.java.AbstractCassandraConfiguration;

/**
 * Created by ijly7474 on 24/08/17.
 */
@TestConfiguration
public class CassandraConfiguration extends AbstractCassandraConfiguration{

    @Value("${spring.data.cassandra.contact-points}")
    private String contactPoints;

    @Value("${spring.data.cassandra.port}")
    private int port;

    @Value("${spring.data.cassandra.username}")
    private String user;

    @Value("${spring.data.cassandra.password}")
    private String password;

    @Override
    public String getKeyspaceName() {
        return "system_auth";
    }

    @Bean
    @Override
    public CassandraClusterFactoryBean cluster() {
        CassandraClusterFactoryBean cluster = new CassandraClusterFactoryBean();
        PlainTextAuthProvider sap = new PlainTextAuthProvider(user, password);
        cluster.setContactPoints(contactPoints);
        cluster.setPort(port);
        cluster.setAuthProvider(sap);
        return cluster;
    }


}
