package com.orange.oss.osb.cassandra.config;

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.servicebroker.model.Catalog;
import org.springframework.cloud.servicebroker.model.Plan;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class CatalogConfig {

	@Value("${catalog_yml}")
	private String catalogYml;

	public String getCatalog(){
		return catalogYml;
	}

	@Bean
	public Catalog catalog() {
		Catalog catalog;
		if (catalogYml == null) { //hard coded catalog is returned
			catalog = new Catalog(Collections.singletonList(
					new ServiceDefinition(
							"cassandra-service-broker",
							"Apache Cassandra database 3.11 for Cloud Foundry",
							"Cassandra key-space on demand on shared cluster",
							true,
							false,
							Collections.singletonList(
									new Plan("cassandra-plan",
											"default",
											"This is a default cassandra plan.  All services are created equally.",
											getPlanMetadata())),
							asList("cassandra", "document"),
							getServiceDefinitionMetadata(),
							null,
							null)));
		}else{
			CatalogYmlReader catalogYmlReader = new CatalogYmlReader();
			List<ServiceDefinition> serviceDefinitions = catalogYmlReader.getServiceDefinitions(catalogYml);
			catalog = new Catalog (serviceDefinitions);
		}
		return catalog;
	}

	private Map<String, Object> getServiceDefinitionMetadata() {
		Map<String, Object> sdMetadata = new HashMap<>();
		sdMetadata.put("displayName", "cassandra");
		sdMetadata.put("imageUrl", "http://cassandra.apache.org/img/cassandra_logo.png");
		sdMetadata.put("longDescription", "Creating a service Cassandra provisions a key-space. Binding applications provisions unique credentials for each application to access the keys-pace");
		sdMetadata.put("providerDisplayName", "Orange");
		sdMetadata.put("documentationUrl", "http://cassandra.apache.org/doc/latest\n");
		sdMetadata.put("supportUrl", "https://contact-us/\n");
		return sdMetadata;
	}

	private Map<String,Object> getPlanMetadata() {
		Map<String,Object> planMetadata = new HashMap<>();
		planMetadata.put("costs", getCosts());
		planMetadata.put("bullets", getBullets());
		return planMetadata;
	}

	private List<Map<String,Object>> getCosts() {
		Map<String,Object> costsMap = new HashMap<>();

		Map<String,Object> amount = new HashMap<>();
		amount.put("eur", 10.0);

		costsMap.put("amount", amount);
		costsMap.put("unit", "MONTHLY");

		return Collections.singletonList(costsMap);
	}

	private List<String> getBullets() {
		return Arrays.asList("Shared cassandra server",
				"100 MB Storage (not enforced)",
				"40 concurrent connections (not enforced)");
	}


}
