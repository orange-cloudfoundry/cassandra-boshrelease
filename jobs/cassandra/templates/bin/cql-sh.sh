#!/bin/bash

set -e # exit immediately if a simple command exits with a non-zero status.
set -u # report the usage of uninitialized variables.

export LANG=en_US.UTF-8

export CASSANDRA_BIN=/var/vcap/packages/cassandra/bin
export CASSANDRA_CONF=/var/vcap/jobs/cassandra_server/conf

export JAVA_HOME=/var/vcap/packages/openjdk
export PATH=$PATH:/var/vcap/packages/openjdk/bin:$CASSANDRA_BIN:$CASSANDRA_CONF

## export CASSANDRA_CONF=/var/vcap/jobs/cassandra_server/conf

export CLIENT_SSL=<%=properties.client_encryption_options.enabled%>

if [[ ${CLIENT_SSL} == "true" ]]
then 
 pushd /var/vcap/packages/cassandra/bin
 exec chpst -u vcap:vcap /var/vcap/packages/cassandra/bin/cqlsh --cqlshrc "/var/vcap/jobs/cassandra/root/.cassandra/cqlshrc" "$@" --ssl
 popd
 exit 0
else 
 pushd /var/vcap/packages/cassandra/bin
 exec chpst -u vcap:vcap /var/vcap/packages/cassandra/bin/cqlsh "$@"
 popd
 exit 0
fi
