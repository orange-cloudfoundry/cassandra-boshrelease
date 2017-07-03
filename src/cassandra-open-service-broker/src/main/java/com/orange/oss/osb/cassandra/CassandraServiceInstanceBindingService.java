package com.orange.oss.osb.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.Session;
import com.orange.oss.osb.cassandra.util.Converter;
import org.apache.commons.text.RandomStringGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.stereotype.Service;

@Service
public class CassandraServiceInstanceBindingService implements ServiceInstanceBindingService  {

	private static final Logger LOGGER = LoggerFactory.getLogger(CassandraServiceInstanceBindingService.class.getName());
	private Cluster cluster = null;

	@Value("${spring.data.cassandra.contact-points}")
	private String contactPoints;

	@Value("${spring.data.cassandra.port}")
	private int port;

	@Value("${spring.data.cassandra.username}")
	private String user;

	@Value("${spring.data.cassandra.password}")
	private String password;

	@Override
	public CreateServiceInstanceBindingResponse createServiceInstanceBinding(CreateServiceInstanceBindingRequest arg0) {
		// TODO use binding_uuid as role name and generate a random password
		// TODO broker exception
		this.createRole(arg0.getBindingId());
		this.grantRoleOnKeyspace(arg0.getServiceInstanceId(), arg0.getBindingId());
		return new CreateServiceInstanceBindingResponse();
	}

	@Override
	public void deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest arg0) {
		// TODO 
		// TODO broker exception
		this.revokeRoleOnKeyspace(arg0.getServiceInstanceId(), arg0.getBindingId());
		this.dropRole(arg0.getBindingId());
	}

	private void createRole(String pRoleName) {

		//TODO : Test if role doesn't exist
		Session session = this.open();
		LOGGER.info("Begin creating Role " + pRoleName);
		String roleNameConverted = Converter.uuidToRoleName(pRoleName);
		LOGGER.info("Role Name converted : " + roleNameConverted);
		String passwordGenerated = this.generatePassword();
		LOGGER.info("Password generated : " + passwordGenerated);
		//session.execute("CREATE ROLE " + roleNameConverted + " WITH PASSWORD = " + "'toto'" + " AND LOGIN = true");
		session.execute("CREATE ROLE " + roleNameConverted + " WITH PASSWORD = '" + passwordGenerated + "' AND LOGIN = true");
		LOGGER.info("End creating Role " + roleNameConverted);
		disconnect(session);
	}

	private void grantRoleOnKeyspace(String pKeyspaceName, String pRoleName) {

		//TODO : Test if keyspace and role exist
		Session session = this.open();
		LOGGER.info("Begin granting Role " + pRoleName + " to Keyspace " + pKeyspaceName);
		String roleNameConverted = Converter.uuidToRoleName(pRoleName);
		String keyspaceNameConverted = Converter.uuidToKeyspaceName(pKeyspaceName);
		LOGGER.info("Role Name converted : " + roleNameConverted);
		LOGGER.info("Keyspace Name converted : " + keyspaceNameConverted);
		session.execute("GRANT ALL PERMISSIONS ON KEYSPACE " + keyspaceNameConverted + " TO " + roleNameConverted );
		LOGGER.info("End granting Role On Keyspace");
		disconnect(session);
	}

	private void dropRole(String pRoleName) {
		Session session = this.open();
		LOGGER.info("Begin dropping Role " + pRoleName);
		String roleNameConverted = Converter.uuidToRoleName(pRoleName);
		LOGGER.info("Role Name converted : " + roleNameConverted);
		session.execute("DROP ROLE " + roleNameConverted);
		LOGGER.info("End creating Role " + roleNameConverted);
		disconnect(session);
	}

	private void revokeRoleOnKeyspace(String pKeyspaceName, String pRoleName) {
		//TODO : Test if role and keyspace exist
		Session session = this.open();
		LOGGER.info("Begin revoking Role " + pRoleName + " from Keyspace " + pKeyspaceName);
		String roleNameConverted = Converter.uuidToRoleName(pRoleName);
		String keyspaceNameConverted = Converter.uuidToKeyspaceName(pKeyspaceName);
		LOGGER.info("Role Name converted : " + roleNameConverted);
		LOGGER.info("Keyspace Name converted : " + keyspaceNameConverted);
		session.execute("REVOKE ALL PERMISSIONS ON KEYSPACE " + keyspaceNameConverted + " FROM " + roleNameConverted );
		LOGGER.info("End revoking Role On Keyspace");
		disconnect(session);
	}

	private Session open(){
		if (this.cluster == null){
//			PlainTextAuthProvider ptap = new PlainTextAuthProvider(user, password);
			this.cluster = Cluster.builder()
					.addContactPoints(contactPoints)
					.withPort(port)
					.withCredentials(user, password)
//					.withAuthProvider(ptap)
					.build();
		}
		return this.cluster.connect();
	}

	private void disconnect(Session session){
		session.close();
	}

	private String generatePassword() {
		RandomStringGenerator generator = new RandomStringGenerator.Builder()
				.withinRange('a', 'z')
				.build();
		return generator.generate(10);
	}
}
