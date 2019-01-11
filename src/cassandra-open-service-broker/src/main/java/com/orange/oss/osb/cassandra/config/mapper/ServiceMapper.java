package com.orange.oss.osb.cassandra.config.mapper;

import com.orange.oss.osb.cassandra.config.model.Service;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Service internal mapper
 */
public class ServiceMapper {

    public static ServiceDefinition toServiceDefinition(Service service) {
        return new ServiceDefinition(service.getId().toString(),
                service.getName(),
                service.getDescription(),
                service.getBindable(),
                service.getPlanUpdateable(),
                PlanMapper.toServiceBrokerPlans(service.getPlans()),
                service.getTags(),
                service.getMetadata(),
                service.getRequires(),
                null);
    }


    public static List<ServiceDefinition> toServiceDefinitions(List<Service> service) {
        List<ServiceDefinition> serviceDefinitionList = new ArrayList<>();
        for (Service sp : service){
            ServiceDefinition serviceDefinition = toServiceDefinition(sp);
            serviceDefinitionList.add(serviceDefinition);
        }
        return serviceDefinitionList;
    }

}
