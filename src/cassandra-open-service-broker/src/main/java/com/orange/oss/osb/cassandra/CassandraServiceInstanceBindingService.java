package com.orange.oss.osb.cassandra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.stereotype.Service;

@Service
public class CassandraServiceInstanceBindingService implements ServiceInstanceBindingService  {

	private static Logger logger=LoggerFactory.getLogger(CassandraServiceInstanceBindingService.class.getName());
	
	@Override
	public CreateServiceInstanceBindingResponse createServiceInstanceBinding(CreateServiceInstanceBindingRequest arg0) {
		// TODO generate a random user / password
		// create user in cassandra
		// associate user to keySpace
		
		return null;
	}

	@Override
	public void deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest arg0) {
		// TODO 
		// dissociate user from keySpace
		// delete keyspace
		
	}

}
