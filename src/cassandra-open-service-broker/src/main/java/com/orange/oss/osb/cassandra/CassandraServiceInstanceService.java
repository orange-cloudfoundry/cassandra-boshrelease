package com.orange.oss.osb.cassandra;

import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.GetLastServiceOperationRequest;
import org.springframework.cloud.servicebroker.model.GetLastServiceOperationResponse;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceResponse;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Service;

import com.orange.oss.osb.cassandra.util.Converter;

@Service
public class CassandraServiceInstanceService implements ServiceInstanceService {

	private static final Logger LOGGER = getLogger(CassandraServiceInstanceService.class);

	@Autowired
	private CassandraTemplate template;

	@Value("${spring.data.cassandra.contact-points}")
	private String contactPoints;

	@Value("${spring.data.cassandra.port}")
	private int port;

	@Value("${spring.data.cassandra.username}")
	private String user;

	@Value("${spring.data.cassandra.password}")
	private String password;

	public CassandraServiceInstanceService(){
	}

	@Override
	public CreateServiceInstanceResponse createServiceInstance(CreateServiceInstanceRequest request) {
		//Create a cassandra keyspace
		createKeyspace(request.getServiceInstanceId());
	    return new CreateServiceInstanceResponse().withDashboardUrl("");
	}

	@Override
	public DeleteServiceInstanceResponse deleteServiceInstance(DeleteServiceInstanceRequest request) {
		//Delete cassandra keyspace
        dropKeyspace(request.getServiceInstanceId());
        return new DeleteServiceInstanceResponse();
	}

	@Override
	public UpdateServiceInstanceResponse updateServiceInstance(UpdateServiceInstanceRequest request) {
		return null;
	}

	@Override
	public GetLastServiceOperationResponse getLastOperation(GetLastServiceOperationRequest request) {
		return null;
	}

	private void createKeyspace(String pKeyspaceName) {
		//TODO : Test if keyspace doesn't exist
		LOGGER.info("Begin creating KeySpace " + pKeyspaceName);
		String keyspaceNameConverted = Converter.uuidToKeyspaceName(pKeyspaceName);
		LOGGER.info("KeySpace Name converted : " + keyspaceNameConverted);
		template.getSession().execute("CREATE KEYSPACE " + keyspaceNameConverted + " WITH REPLICATION " + "= {'class':'SimpleStrategy', 'replication_factor': 3};");
		LOGGER.info("End creating KeySpace " + keyspaceNameConverted);
	}

    private void dropKeyspace(String pKeyspaceName) {
		//TODO : Test if keyspace exists
        LOGGER.info("Begin deleting KeySpace " + pKeyspaceName);
		String keyspaceNameConverted = Converter.uuidToKeyspaceName(pKeyspaceName);
		LOGGER.info("KeySpace Name converted : " + keyspaceNameConverted);
		template.getSession().execute("DROP KEYSPACE " + keyspaceNameConverted);
        LOGGER.info("End deleting KeySpace " + keyspaceNameConverted);
    }
}
