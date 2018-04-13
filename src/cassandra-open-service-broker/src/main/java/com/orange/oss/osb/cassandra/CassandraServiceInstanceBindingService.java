package com.orange.oss.osb.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.orange.oss.osb.cassandra.util.Converter;
import org.apache.commons.text.RandomStringGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CassandraServiceInstanceBindingService implements ServiceInstanceBindingService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CassandraServiceInstanceBindingService.class.getName());
	//private Cluster cluster = null;

	@Autowired
	private CassandraTemplate template;

	@Value("${spring.data.cassandra.contact-points}")
	private String contactPoints;

	@Value("${spring.data.cassandra.port}")
	private int port;

	@Value("${spring.data.cassandra.ssl}")
	private boolean ssl;

	@Value("${spring.data.cassandra.username}")
	private String user;

	@Value("${spring.data.cassandra.password}")
	private String password;

	@Override
	public CreateServiceInstanceBindingResponse createServiceInstanceBinding(CreateServiceInstanceBindingRequest arg0) {
		//Create role
		String passwordGenerated = this.createRole(arg0.getBindingId());
		//Grant role
		this.grantRoleOnKeyspace(arg0.getServiceInstanceId(), arg0.getBindingId());
		//Build credentials
		CreateServiceInstanceAppBindingResponse createServiceInstanceAppBindingResponse = new CreateServiceInstanceAppBindingResponse();
		Map<String, Object> credentials = Converter.buildCredentials(this.contactPoints, String.valueOf(port), ssl, arg0.getServiceInstanceId(), arg0.getBindingId(), passwordGenerated);
		return createServiceInstanceAppBindingResponse.withCredentials(credentials);

	}

	@Override
	public void deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest arg0) {
		// TODO broker exception
		this.revokeRoleOnKeyspace(arg0.getServiceInstanceId(), arg0.getBindingId());
		this.dropRole(arg0.getBindingId());
	}

	private String createRole(String pRoleName) {

		//TODO : Test if role doesn't exist
		//Session session = this.open();
		LOGGER.info("Begin creating Role " + pRoleName);
		String roleNameConverted = Converter.uuidToRoleName(pRoleName);
		LOGGER.info("Role Name converted : " + roleNameConverted);
		String passwordGenerated = this.generatePassword();
		LOGGER.info("Password generated : " + passwordGenerated);
		this.template.getSession().execute("CREATE ROLE " + roleNameConverted + " WITH PASSWORD = '" + passwordGenerated + "' AND LOGIN = true");
		LOGGER.info("End creating Role " + roleNameConverted);
		//disconnect(session);
		return passwordGenerated;
	}

	private void grantRoleOnKeyspace(String pKeyspaceName, String pRoleName) {

		//TODO : Test if keyspace and role exist
		//Session session = this.open();
		LOGGER.info("Begin granting Role " + pRoleName + " to Keyspace " + pKeyspaceName);
		String roleNameConverted = Converter.uuidToRoleName(pRoleName);
		String keyspaceNameConverted = Converter.uuidToKeyspaceName(pKeyspaceName);
		LOGGER.info("Role Name converted : " + roleNameConverted);
		LOGGER.info("Keyspace Name converted : " + keyspaceNameConverted);
		this.template.getSession().execute("GRANT ALL PERMISSIONS ON KEYSPACE " + keyspaceNameConverted + " TO " + roleNameConverted);
		LOGGER.info("End granting Role On Keyspace");
		//disconnect(session);
	}

	private void dropRole(String pRoleName) {
		//Session session = this.open();
		LOGGER.info("Begin dropping Role " + pRoleName);
		String roleNameConverted = Converter.uuidToRoleName(pRoleName);
		LOGGER.info("Role Name converted : " + roleNameConverted);
		this.template.getSession().execute("DROP ROLE " + roleNameConverted);
		LOGGER.info("End creating Role " + roleNameConverted);
		//disconnect(session);
	}

	private void revokeRoleOnKeyspace(String pKeyspaceName, String pRoleName) {
		//TODO : Test if role and keyspace exist
		//Session session = this.open();
		LOGGER.info("Begin revoking Role " + pRoleName + " from Keyspace " + pKeyspaceName);
		String roleNameConverted = Converter.uuidToRoleName(pRoleName);
		String keyspaceNameConverted = Converter.uuidToKeyspaceName(pKeyspaceName);
		LOGGER.info("Role Name converted : " + roleNameConverted);
		LOGGER.info("Keyspace Name converted : " + keyspaceNameConverted);
		this.template.getSession().execute("REVOKE ALL PERMISSIONS ON KEYSPACE " + keyspaceNameConverted + " FROM " + roleNameConverted);
		LOGGER.info("End revoking Role On Keyspace");
		//disconnect(session);
	}

//	private Session open() {
//		if (this.cluster == null) {
//			PlainTextAuthProvider ptap = new PlainTextAuthProvider(user, password);
//			this.cluster = Cluster.builder()
//					.addContactPoints(contactPoints)
//					.withPort(port)
//					.withCredentials(user, password)
//					.withAuthProvider(ptap)
//					.build();
//		}
//		return this.cluster.connect();
//	}

//	private void disconnect(Session session) {
//		session.close();
//	}

	private String generatePassword() {
		RandomStringGenerator generator = new RandomStringGenerator.Builder()
				.withinRange('a', 'z')
				.build();
		return generator.generate(10);
	}

}