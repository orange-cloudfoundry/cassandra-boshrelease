package com.orange.oss.osb.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.orange.oss.osb.cassandra.helper.TestCassandraConfiguration;
import com.orange.oss.osb.cassandra.util.Converter;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.servicebroker.model.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class CassandraOpenServiceBrokerApplicationTests {

    @Autowired
    private CatalogConfig catalogConfig;

    @Autowired
    private CassandraServiceInstanceService cassandraServiceInstanceService;

    @Autowired
    private CassandraServiceInstanceBindingService cassandraServiceInstanceBindingService;

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraOpenServiceBrokerApplicationTests.class.getName());
    private static final String SERVICE_INSTANCE_UUID = "055d0899-018d-4841-ba66-2e4d4ce91f47";
    private static final String APP_UUID = "aaaaaaaa-ba66-4841-018d-2e4d4ce91f47";
    private static final String BINDING_UUID = "bbbbbbbb-ba66-4841-018d-2e4d4ce91f47";
    private static final String SELECT_KEYSPACES = "select * from system_schema.keyspaces";
    private static final String SELECT_KEYSPACE_BY_NAME = "select * from system_schema.keyspaces where keyspace_name = ";
    private static final String LIST_ROLES = "LIST ROLES";
    private static final String LIST_PERMISSIONS_OF_A_ROLE = "LIST ALL OF ";
    private static final String LIST_ALL_PERMISSIONS = "LIST ALL";
    private static final int EXPECTED_REVOKED_PERMISSIONS = 0;
    private static final int EXPECTED_GRANTED_PERMISSIONS = 6;


//    @Before
//    public void clean() {
//        this.dropKeyspace();
//        this.dropRole();
//    }

    @Test
    public void createServiceInstanceShouldCreateNewKeyspaceInCassandra() {

        //Given :
        CreateServiceInstanceRequest createServiceInstanceRequest = new CreateServiceInstanceRequest(
                catalogConfig.catalog().getServiceDefinitions().get(0).getId(),
                catalogConfig.catalog().getServiceDefinitions().get(0).getPlans().get(0).getId(),
                "myOrganization",
                "mySpace"
        );
        createServiceInstanceRequest.withServiceInstanceId(SERVICE_INSTANCE_UUID);
        int keyspacesCounterBefore = this.countKeyspaces();

        //When :
        CreateServiceInstanceResponse createServiceInstanceResponse = this.cassandraServiceInstanceService.createServiceInstance(createServiceInstanceRequest);

        //Then :
        //Assert that keyspace is created in Cassandra (by default should have one more keyspace)
        int keyspacesCounterAfter = this.countKeyspaces();
        assertEquals("Keyspaces counter", 1, keyspacesCounterAfter - keyspacesCounterBefore);
        assertNotNull("Keyspace existence", this.selectKeyspaceByName(Converter.uuidToKeyspaceName(SERVICE_INSTANCE_UUID)));

        //Clean up keyspace
        this.dropKeyspace();
    }

    @Test
    public void deleteServiceInstanceShouldDropExistingKeyspaceInCassandra() {

        //Init
        this.createKeyspace();

        //Given
        DeleteServiceInstanceRequest deleteServiceInstanceRequest = new DeleteServiceInstanceRequest(
                SERVICE_INSTANCE_UUID,
                catalogConfig.catalog().getServiceDefinitions().get(0).getId(),
                catalogConfig.catalog().getServiceDefinitions().get(0).getPlans().get(0).getId(),
                catalogConfig.catalog().getServiceDefinitions().get(0)
        );
        int keyspacesCounterBefore = this.countKeyspaces();

        //When :
        DeleteServiceInstanceResponse deleteServiceInstanceResponse = this.cassandraServiceInstanceService.deleteServiceInstance(deleteServiceInstanceRequest);


        //Then :
        //Assert that keyspace is dropped from Cassandra (by default should have one less keyspace)
        int keyspacesCounterAfter = this.countKeyspaces();
        assertEquals("Keyspaces counter", -1, keyspacesCounterAfter - keyspacesCounterBefore);
        assertNull("Keyspace non existence", this.selectKeyspaceByName(Converter.uuidToKeyspaceName(SERVICE_INSTANCE_UUID)));

    }

    @Test
    public void createServiceInstanceBindingShouldCreateARoleInCassandraAndGrantTheRoleAllPermissionsToKeyspace(){

        //Given :
        this.createKeyspace();
        CreateServiceInstanceBindingRequest createServiceInstanceBindingRequest = new CreateServiceInstanceBindingRequest(
                catalogConfig.catalog().getServiceDefinitions().get(0).getId(),
                catalogConfig.catalog().getServiceDefinitions().get(0).getPlans().get(0).getId(),
                APP_UUID,
                initBindResources()
        );
        createServiceInstanceBindingRequest.withServiceInstanceId(SERVICE_INSTANCE_UUID);
        createServiceInstanceBindingRequest.withBindingId(BINDING_UUID);
        int rolesCounterBefore = this.countRoles();

        //When :
        CreateServiceInstanceBindingResponse createServiceInstanceBindingResponse = this.cassandraServiceInstanceBindingService.createServiceInstanceBinding(createServiceInstanceBindingRequest);

        //Then :
        // Assert that the role is created in Cassandra
        int rolesCounterAfter = this.countRoles();
        assertEquals("Roles counter", 1, rolesCounterAfter - rolesCounterBefore);
        // Assert that the permissions are granted to role on keyspace
        assertEquals("Permissions counter", EXPECTED_GRANTED_PERMISSIONS, this.countPermissionsOnKeyspace());
        // Assert non null expected Credentials (deeper unit testing is performed in ConverterTest)
        CreateServiceInstanceAppBindingResponse createServiceInstanceAppBindingResponse = (CreateServiceInstanceAppBindingResponse)createServiceInstanceBindingResponse;
        Map<String, Object> credentials = createServiceInstanceAppBindingResponse.getCredentials();
        assertNotNull("Credentials are null", credentials);
        assertNotNull("contact-points are null", credentials.get("contact-points"));
        assertNotNull("port is null", credentials.get("port"));
        assertNotNull("login is null", credentials.get("login"));
        assertNotNull("password is null", credentials.get("password"));
        assertNotNull("jdbcUrl is null", credentials.get("jdbcUrl"));

        //Clean up permissions, roles and keyspace
        this.revokePermissions();
        this.dropRole();
        this.dropKeyspace();
    }


    @Test
    public void deleteServiceInstanceBindingShouldRevokeTheRoleAllPermissionsToKeyspaceAndDropTheRole(){

        //Init
        this.createKeyspace();
        this.createRole();
        this.grantPermissions();

        //Given :
        DeleteServiceInstanceBindingRequest deleteServiceInstanceBindingRequest = new DeleteServiceInstanceBindingRequest(
                SERVICE_INSTANCE_UUID,
                BINDING_UUID,
                catalogConfig.catalog().getServiceDefinitions().get(0).getId(),
                catalogConfig.catalog().getServiceDefinitions().get(0).getPlans().get(0).getId(),
                catalogConfig.catalog().getServiceDefinitions().get(0)
        );
        int rolesCounterBefore = this.countRoles();

        //When :
        this.cassandraServiceInstanceBindingService.deleteServiceInstanceBinding(deleteServiceInstanceBindingRequest);

        //Then :
        // Assert that the permissions are revoked dropped from Cassandra
        assertEquals("Permissions counter", EXPECTED_REVOKED_PERMISSIONS, this.countAllPermissionsOfARole());

        // Assert that the role is dropped from Cassandra
        int rolesCounterAfter = this.countRoles();
        assertEquals("Roles counter", -1, rolesCounterAfter - rolesCounterBefore);

        //Clean up
        this.dropKeyspace();
    }


    //TODO : Tests des cas limites







    private Map initBindResources(){
        Map bindResources = new HashMap<String, String>();
        bindResources.put("app_guid", APP_UUID);
        return bindResources;
    }

    private int countKeyspaces() {
        ResultSet results = TestCassandraConfiguration.session.execute(SELECT_KEYSPACES);
        int counter = 0;
        for (Row row : results.all()) {
            String ksName = row.getString("keyspace_name");
            LOGGER.info("######" + ksName);
            counter++;
        }
        return counter;
    }

    private Row selectKeyspaceByName(String pKeyspaceName) {
        String query = SELECT_KEYSPACE_BY_NAME + "'" + pKeyspaceName + "'";
        ResultSet results = TestCassandraConfiguration.session.execute(query);
        return results.one();
    }

    private int countRoles() {
        ResultSet results = TestCassandraConfiguration.session.execute(LIST_ROLES);
        int counter = 0;
        for (Row row : results.all()) {
            String roleName = row.getString("role");
            LOGGER.info("###### Role" + roleName);
            counter++;
        }
        return counter;
    }

    private int countAllPermissionsOfARole() {
        String roleNameConverted = Converter.uuidToRoleName(BINDING_UUID);

        ResultSet results = TestCassandraConfiguration.session.execute(LIST_ALL_PERMISSIONS);
        int counter = 0;
        for (Row row : results.all()) {
            String role = row.getString("role");
            LOGGER.info("###### Role" + role);
            if (role.contains(roleNameConverted)){
                counter++;
            }
        }
        return counter;
    }

    private int countPermissionsOnKeyspace() {
        String keyspaceNameConverted = Converter.uuidToKeyspaceName(SERVICE_INSTANCE_UUID);
        String roleNameConverted = Converter.uuidToRoleName(BINDING_UUID);

        ResultSet results = TestCassandraConfiguration.session.execute(LIST_PERMISSIONS_OF_A_ROLE + roleNameConverted);
        int counter = 0;
        for (Row row : results.all()) {
            String resource = row.getString("resource");
            LOGGER.info("###### Resource" + resource);
            if (resource.contains(keyspaceNameConverted)){
                counter++;
            }
        }
        return counter;
    }




    private void createKeyspace(){
        String keyspaceNameConverted = Converter.uuidToKeyspaceName(SERVICE_INSTANCE_UUID);
        LOGGER.info("KeySpace Name converted : " + keyspaceNameConverted);
        TestCassandraConfiguration.session.execute("CREATE KEYSPACE " + keyspaceNameConverted + " WITH REPLICATION " + "= {'class':'SimpleStrategy', 'replication_factor': 3};");
    }

    private void dropKeyspace(){
        String keyspaceNameConverted = Converter.uuidToKeyspaceName(SERVICE_INSTANCE_UUID);
        LOGGER.info("KeySpace Name converted : " + keyspaceNameConverted);
        TestCassandraConfiguration.session.execute("DROP KEYSPACE " + keyspaceNameConverted);
    }

    private void createRole(){
        String roleNameConverted = Converter.uuidToRoleName(BINDING_UUID);
        LOGGER.info("Role Name converted : " + roleNameConverted);
        TestCassandraConfiguration.session.execute("CREATE ROLE " + roleNameConverted);
    }

    private void dropRole(){
        String roleNameConverted = Converter.uuidToRoleName(BINDING_UUID);
        LOGGER.info("Role Name converted : " + roleNameConverted);
        TestCassandraConfiguration.session.execute("DROP ROLE " + roleNameConverted);
    }

    private void grantPermissions(){
        String keyspaceNameConverted = Converter.uuidToKeyspaceName(SERVICE_INSTANCE_UUID);
        String roleNameConverted = Converter.uuidToRoleName(BINDING_UUID);
        LOGGER.info("Keyspace Name converted : " + keyspaceNameConverted);
        LOGGER.info("Role Name converted : " + roleNameConverted);
        TestCassandraConfiguration.session.execute("GRANT ALL PERMISSIONS ON KEYSPACE " + keyspaceNameConverted + " TO " + roleNameConverted);
    }

    private void revokePermissions(){
        String keyspaceNameConverted = Converter.uuidToKeyspaceName(SERVICE_INSTANCE_UUID);
        String roleNameConverted = Converter.uuidToRoleName(BINDING_UUID);
        LOGGER.info("Keyspace Name converted : " + keyspaceNameConverted);
        LOGGER.info("Role Name converted : " + roleNameConverted);
        TestCassandraConfiguration.session.execute("REVOKE ALL PERMISSIONS ON KEYSPACE " + keyspaceNameConverted + " FROM " + roleNameConverted);
    }



}
