#!/bin/sh
wget https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/0.1.0/jmx_prometheus_javaagent-0.1.0.jar
wget http://archive.apache.org/dist/cassandra/3.9/apache-cassandra-3.9-bin.tar.gz
wget https://s3-us-west-1.amazonaws.com/cf-cli-releases/releases/v6.30.0/cf-cli_6.30.0_linux_x86-64.tgz
git clone https://github.com/JCL38-ORANGE/cf-cassandra-example-app.git
wget https://github.com/criteo/cassandra_exporter/releases/download/2.1.0/cassandra_exporter-2.1.0.jar
