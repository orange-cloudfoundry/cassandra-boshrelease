#!/bin/bash

#set -e # exit immediately if a simple command exits with a non-zero status.
set -u # report the usage of uninitialized variables.

export LANG=en_US.UTF-8

export CASSANDRA_BIN=/var/vcap/packages/cassandra/bin
export CASSANDRA_CONF=/var/vcap/jobs/cassandra_server/conf

export JAVA_HOME=/var/vcap/packages/openjdk
export PATH=$PATH:/var/vcap/packages/openjdk/bin:$CASSANDRA_BIN:$CASSANDRA_CONF

export CASS_PWD="<%=properties.cassandra_server.cass_pwd%>"
## export CASSANDRA_CONF=/var/vcap/jobs/cassandra_server/conf

/var/vcap/packages/cassandra/bin/cqlsh `hostname -I` -e "alter role cassandra with password = '$CASS_PWD' " -u cassandra -p cassandra 2>&1>/dev/null
if [[ "$?" == 1 ]]; then
/var/vcap/packages/cassandra/bin/cqlsh `hostname -I` -e "alter role cassandra with password = '$CASS_PWD' " -u cassandra -p $CASS_PWD 2>&1>/dev/null
fi
/var/vcap/packages/cassandra/bin/cqlsh `hostname -I` -e "alter keyspace system_auth WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '3'}  AND durable_writes = true" -u cassandra -p $CASS_PWD  2>&1>/dev/null
/var/vcap/jobs/cassandra_seed/bin/./node-tool.sh repair system_auth
#exit 0

/var/vcap/jobs/cassandra_server/bin/./creer_pem_cli_serv.sh
exit 0
