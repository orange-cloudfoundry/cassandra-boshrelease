package com.orange.oss.osb.cassandra.cassandraunit;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.cassandraunit.spring.CassandraUnitDependencyInjectionTestExecutionListener;
import org.cassandraunit.spring.CassandraUnitTestExecutionListener;
import org.cassandraunit.spring.EmbeddedCassandra;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test_embedded")
@TestExecutionListeners(listeners = {CassandraUnitDependencyInjectionTestExecutionListener.class, CassandraUnitTestExecutionListener.class, DependencyInjectionTestExecutionListener.class})
@EmbeddedCassandra(timeout = 50000L)
@SpringBootTest
@Import(CassandraConfiguration.class)
//@OverrideAutoConfiguration(enabled = false)
//@AutoConfigureWebMvc

public class CassandraOpenServiceBrokerApplicationTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraOpenServiceBrokerApplicationTests.class.getName());
    private static final String SELECT_KEYSPACES = "select * from system_schema.keyspaces";


    @Test
    public void test(){
        //LOGGER.info("Begin connecting...");
        //Cluster cluster = Cluster.builder().addContactPoints("127.0.0.1").withCredentials("cassandra", "cassandra").withPort(9142).build();
        //Session session = cluster.connect();
        //ResultSet results = session.execute(SELECT_KEYSPACES);
        //for (Row row : results.all()) {
        //    String ksName = row.getString("keyspace_name");
        //    LOGGER.info("######" + ksName);
        //}
    }

}
