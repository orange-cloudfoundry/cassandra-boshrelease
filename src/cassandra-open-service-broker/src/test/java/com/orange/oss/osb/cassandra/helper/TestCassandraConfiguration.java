package com.orange.oss.osb.cassandra.helper;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.Session;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.cassandra.core.CassandraTemplate;

import java.io.IOException;
import java.time.Duration;

@TestConfiguration
public class TestCassandraConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestCassandraConfiguration.class);

    @Bean
    public Session embedded() {
        LOGGER.info("Starting embedded cassandra server");
        try {
            EmbeddedCassandraServerHelper.startEmbeddedCassandra("cu-cassandra.yaml",
                    Duration.ofSeconds(50l).toMillis());
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("Embedded cassandra server started");
        Cluster cluster = Cluster.builder().addContactPoints("127.0.0.1").withPort(9142).build();
        return cluster.connect();
    }
}
