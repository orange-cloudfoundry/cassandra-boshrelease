package com.orange.oss.osb.cassandra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

	
	
	
	private static Logger logger=LoggerFactory.getLogger(CassandraServiceInstanceService.class.getName());
	
	@Autowired
	CqlTemplate template;
	
	@Autowired
	public CassandraServiceInstanceService(){
		
	}
	
	@Override
	public CreateServiceInstanceResponse createServiceInstance(CreateServiceInstanceRequest arg0) {
		// TODO create a cassandra keyspace
		// TODO: persist the created service instance in cassandra broker-keyspace.
		//{'class':'SimpleStrategy', 'replication_factor' : 3};
		
		
//		template.execute("CREATE KEYSPACE  _\“KeySpace Name\” WITH replication = {'class': ‘SimpleStrategy’, 'replication_factor' : ‘3’};");
		
		return null;
	}

	@Override
	public DeleteServiceInstanceResponse deleteServiceInstance(DeleteServiceInstanceRequest arg0) {
		// TODO delete cassandra keyspace
		return null;
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
	
	
	
}
