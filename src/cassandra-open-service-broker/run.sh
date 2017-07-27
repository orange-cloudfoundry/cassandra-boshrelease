#!/bin/bash

java -Dspring.config.location=file:target/application.yml -jar target/cassandra-open-service-broker-0.0.1-SNAPSHOT.jar
