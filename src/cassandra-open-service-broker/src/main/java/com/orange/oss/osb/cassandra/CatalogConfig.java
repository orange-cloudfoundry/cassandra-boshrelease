package com.orange.oss.osb.cassandra;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.servicebroker.model.Catalog;
import org.springframework.cloud.servicebroker.model.Plan;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

@Service
public class CatalogConfig {
	@Bean
	public Catalog catalog() {
		return new Catalog(Collections.singletonList(
				new ServiceDefinition(
						"cassandra-service-broker",
						"cassandra",
						"A simple cassandra service broker implementation",
						true,
						false,
						Collections.singletonList(
								new Plan("cassandra-plan",
										"default",
										"This is a default cassandra plan.  All services are created equally.",
										getPlanMetadata())),
						Arrays.asList("cassandra", "document"),
						getServiceDefinitionMetadata(),
						null,
						null)));
	}


	private Map<String, Object> getServiceDefinitionMetadata() {
		Map<String, Object> sdMetadata = new HashMap<>();
		sdMetadata.put("displayName", "cassandra");
		sdMetadata.put("imageUrl", "http://cassandra.apache.org/img/cassandra_logo.png");
		sdMetadata.put("longDescription", "cassandra Service");
		sdMetadata.put("providerDisplayName", "Orange");
		sdMetadata.put("documentationUrl", "https://github.com/orange-cloudfoundry/cassandra-cf-service-boshrelease\n");
		sdMetadata.put("supportUrl", "https://github.com/orange-cloudfoundry/cassandra-cf-service-boshrelease\n");
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
		amount.put("usd", 0.0);

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
