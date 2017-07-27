package com.orange.oss.osb.cassandra.helper;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.Session;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.cassandra.core.CassandraTemplate;

import java.time.Duration;

//@Configuration
//public class TestCassandraConfiguration implements DisposableBean{
public class TestCassandraConfiguration{
    private static final Logger LOGGER = LoggerFactory.getLogger(TestCassandraConfiguration.class);

    @Value("${cassandra.startupTimeoutInSeconds}")
    private long startupTimeoutInSeconds;

    @Value("${spring.data.cassandra.contact-points}")
    private String contactPoints;

    @Value("${spring.data.cassandra.port}")
    private int port;

    @Value("${spring.data.cassandra.username}")
    private String user;

    @Value("${spring.data.cassandra.password}")
    private String password;

    public static Cluster cluster;
    public static Session session;

    @Bean
    public Session session() throws Exception {
        if (session == null) {
            initialize();
        }
        return session;
    }

//    @Override
    public void destroy() throws Exception {
        if (cluster != null) {
            cluster.close();
            cluster = null;
        }
    }

    private void initialize() throws Exception {
        //LOGGER.info("Starting embedded cassandra server");
        //EmbeddedCassandraServerHelper.startEmbeddedCassandra("cu-cassandra.yaml",
        //        Duration.ofSeconds(startupTimeoutInSeconds).toMillis());
        //LOGGER.info("Embedded cassandra server started");

        LOGGER.info("Connecting to Cassandra... ");
        //PlainTextAuthProvider ptap = new PlainTextAuthProvider(user, password);
        cluster = Cluster.builder()
                        .addContactPoints(contactPoints)
                        .withPort(port)
                        .withCredentials(user, password)
        //                .withAuthProvider(ptap)
                        .build();
        session = cluster.connect();
    }
}
