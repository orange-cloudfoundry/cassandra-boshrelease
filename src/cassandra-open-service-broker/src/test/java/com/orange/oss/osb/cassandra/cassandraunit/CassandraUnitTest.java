package com.orange.oss.osb.cassandra.cassandraunit;

import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by ijly7474 on 24/08/17.
 */
@Ignore
public class CassandraUnitTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraOpenServiceBrokerApplicationTests.class.getName());


    @Test
    public void test(){
        try {
            LOGGER.info("Starting...");
            EmbeddedCassandraServerHelper.startEmbeddedCassandra("cu-cassandrastandalone.yaml");
            LOGGER.info("Started");
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }



}
