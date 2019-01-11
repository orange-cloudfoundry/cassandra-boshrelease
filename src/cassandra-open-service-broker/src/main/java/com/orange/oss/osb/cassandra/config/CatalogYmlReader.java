package com.orange.oss.osb.cassandra.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.servicebroker.model.ServiceDefinition;
import com.orange.oss.osb.cassandra.config.mapper.ServiceMapper;
import com.orange.oss.osb.cassandra.config.model.Catalog;

import java.io.IOException;
import java.util.List;

public class CatalogYmlReader {

    private Logger logger = LoggerFactory.getLogger(CatalogYmlReader.class);

    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    protected ObjectMapper getMapper() {
        return mapper;
    }

    public List<ServiceDefinition> getServiceDefinitions(String catalogYml) {
        List<ServiceDefinition> serviceDefinitions = null;
        try {
            Catalog catalog = getMapper().readValue(catalogYml, Catalog.class);
            serviceDefinitions = ServiceMapper.toServiceDefinitions(catalog.getServices());
        } catch (IOException e) {
            logger.error("Catalog reader fails : " + catalogYml);
        }
        return serviceDefinitions;
    }
}
