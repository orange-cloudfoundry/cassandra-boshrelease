package com.orange.oss.osb.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.Session;
import com.orange.oss.osb.cassandra.util.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cassandra.core.CqlTemplate;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceResponse;
import org.springframework.cloud.servicebroker.model.GetLastServiceOperationRequest;
import org.springframework.cloud.servicebroker.model.GetLastServiceOperationResponse;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceRequest;
import org.springframework.cloud.servicebroker.model.UpdateServiceInstanceResponse;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.stereotype.Service;


@Service
public class CassandraServiceInstanceService implements ServiceInstanceService {


	private static final Logger LOGGER = LoggerFactory.getLogger(CassandraServiceInstanceService.class.getName());
	private Cluster cluster = null;

//	@Autowired
//	CqlTemplate template;

	@Value("${spring.data.cassandra.contact-points}")
	private String contactPoints;

	@Value("${spring.data.cassandra.port}")
	private int port;

	@Value("${spring.data.cassandra.username}")
	private String user;

	@Value("${spring.data.cassandra.password}")
	private String password;



	@Autowired
	public CassandraServiceInstanceService(){
	}

	@Override
	public CreateServiceInstanceResponse createServiceInstance(CreateServiceInstanceRequest arg0) {
		//Create a cassandra keyspace
		this.createKeyspace(arg0.getServiceInstanceId());
	    return new CreateServiceInstanceResponse();
	}

	@Override
	public DeleteServiceInstanceResponse deleteServiceInstance(DeleteServiceInstanceRequest arg0) {
		//Delete cassandra keyspace
        this.dropKeyspace(arg0.getServiceInstanceId());
        return new DeleteServiceInstanceResponse();
	}

	@Override
	public UpdateServiceInstanceResponse updateServiceInstance(UpdateServiceInstanceRequest arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GetLastServiceOperationResponse getLastOperation(GetLastServiceOperationRequest arg0) {
		// TODO Auto-generated method stub
		return null;
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

	private void createKeyspace(String pKeyspaceName) {

		//TODO : Test if keyspace doesn't exist
		Session session = this.open();
		LOGGER.info("Begin creating KeySpace " + pKeyspaceName);
		String keyspaceNameConverted = Converter.uuidToKeyspaceName(pKeyspaceName);
		LOGGER.info("KeySpace Name converted : " + keyspaceNameConverted);
		session.execute("CREATE KEYSPACE " + keyspaceNameConverted + " WITH REPLICATION " + "= {'class':'SimpleStrategy', 'replication_factor': 3};");
		LOGGER.info("End creating KeySpace " + keyspaceNameConverted);
        disconnect(session);
	}

    private void dropKeyspace(String pKeyspaceName) {

		//TODO : Test if keyspace exists
		Session session = this.open();
        LOGGER.info("Begin deleting KeySpace " + pKeyspaceName);
		String keyspaceNameConverted = Converter.uuidToKeyspaceName(pKeyspaceName);
		LOGGER.info("KeySpace Name converted : " + keyspaceNameConverted);
        session.execute("DROP KEYSPACE " + keyspaceNameConverted);
        LOGGER.info("End deleting KeySpace " + keyspaceNameConverted);
		disconnect(session);
    }
}
