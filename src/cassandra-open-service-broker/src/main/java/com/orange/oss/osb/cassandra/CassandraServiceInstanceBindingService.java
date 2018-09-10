package com.orange.oss.osb.cassandra;

import static com.orange.oss.osb.cassandra.util.Converter.buildCredentials;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;

import org.apache.commons.text.RandomStringGenerator;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceAppBindingResponse;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Service;

import com.orange.oss.osb.cassandra.util.Converter;

@Service
public class CassandraServiceInstanceBindingService implements ServiceInstanceBindingService {

	private static final Logger LOGGER = getLogger(CassandraServiceInstanceBindingService.class);

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
	public CreateServiceInstanceBindingResponse createServiceInstanceBinding(CreateServiceInstanceBindingRequest request) {
		//Create role
		String password = createRole(request.getBindingId());
		//Grant role
		grantRoleOnKeyspace(request.getServiceInstanceId(), request.getBindingId());
		//Build credentials
		CreateServiceInstanceAppBindingResponse response = new CreateServiceInstanceAppBindingResponse();
		Map<String, Object> credentials = buildCredentials(contactPoints, String.valueOf(port), ssl, request.getServiceInstanceId(), request.getBindingId(), password);
		return response.withCredentials(credentials);

	}

	@Override
	public void deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest request) {
		// TODO broker exception
		revokeRoleOnKeyspace(request.getServiceInstanceId(), request.getBindingId());
		dropRole(request.getBindingId());
	}

	private String createRole(String pRoleName) {
		//TODO : Test if role doesn't exist
		LOGGER.info("Begin creating Role " + pRoleName);
		String roleNameConverted = Converter.uuidToRoleName(pRoleName);
		LOGGER.info("Role Name converted : " + roleNameConverted);
		String password = generatePassword();
		LOGGER.info("Password generated : " + password);
		template.getSession().execute("CREATE ROLE " + roleNameConverted + " WITH PASSWORD = '" + password + "' AND LOGIN = true");
		LOGGER.info("End creating Role " + roleNameConverted);
		return password;
	}

	private void grantRoleOnKeyspace(String pKeyspaceName, String pRoleName) {
		//TODO : Test if keyspace and role exist
		LOGGER.info("Begin granting Role " + pRoleName + " to Keyspace " + pKeyspaceName);
		String roleNameConverted = Converter.uuidToRoleName(pRoleName);
		String keyspaceNameConverted = Converter.uuidToKeyspaceName(pKeyspaceName);
		LOGGER.info("Role Name converted : " + roleNameConverted);
		LOGGER.info("Keyspace Name converted : " + keyspaceNameConverted);
		template.getSession().execute("GRANT ALL PERMISSIONS ON KEYSPACE " + keyspaceNameConverted + " TO " + roleNameConverted);
		LOGGER.info("End granting Role On Keyspace");
	}

	private void dropRole(String pRoleName) {
		LOGGER.info("Begin dropping Role " + pRoleName);
		String roleNameConverted = Converter.uuidToRoleName(pRoleName);
		LOGGER.info("Role Name converted : " + roleNameConverted);
		template.getSession().execute("DROP ROLE " + roleNameConverted);
		LOGGER.info("End creating Role " + roleNameConverted);
	}

	private void revokeRoleOnKeyspace(String pKeyspaceName, String pRoleName) {
		//TODO : Test if role and keyspace exist
		LOGGER.info("Begin revoking Role " + pRoleName + " from Keyspace " + pKeyspaceName);
		String roleNameConverted = Converter.uuidToRoleName(pRoleName);
		String keyspaceNameConverted = Converter.uuidToKeyspaceName(pKeyspaceName);
		LOGGER.info("Role Name converted : " + roleNameConverted);
		LOGGER.info("Keyspace Name converted : " + keyspaceNameConverted);
		template.getSession().execute("REVOKE ALL PERMISSIONS ON KEYSPACE " + keyspaceNameConverted + " FROM " + roleNameConverted);
		LOGGER.info("End revoking Role On Keyspace");
	}

	private String generatePassword() {
		RandomStringGenerator generator = new RandomStringGenerator.Builder()
				.withinRange('a', 'z')
				.build();
		return generator.generate(10);
	}

}