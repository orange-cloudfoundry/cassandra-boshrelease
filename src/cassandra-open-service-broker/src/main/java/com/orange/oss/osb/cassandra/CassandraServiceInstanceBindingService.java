package com.orange.oss.osb.cassandra;

import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.stereotype.Service;

@Service
public class CassandraServiceInstanceBindingService implements ServiceInstanceBindingService  {

	@Override
	public CreateServiceInstanceBindingResponse createServiceInstanceBinding(CreateServiceInstanceBindingRequest arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteServiceInstanceBinding(DeleteServiceInstanceBindingRequest arg0) {
		// TODO Auto-generated method stub
		
	}

}
