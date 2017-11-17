#!/bin/bash

set -x
#set -e # exit immediately if a simple command exits with a non-zero status.
set -u # report the usage of uninitialized variables.

export LANG=en_US.UTF-8

export CASSANDRA_BIN=/var/vcap/packages/cassandra/bin
export CASSANDRA_CONF=/var/vcap/jobs/cassandra_seed/conf

export JAVA_HOME=/var/vcap/packages/openjdk
export PATH=$PATH:/var/vcap/packages/openjdk/bin:$CASSANDRA_BIN:$CASSANDRA_CONF

export CASS_PWD="<%=properties.cassandra_seed.cass_pwd%>"
## export CASSANDRA_CONF=/var/vcap/jobs/cassandra_seed/conf

export CLIENT_SSL=<%=properties.cassandra_seed.client_encryption_options.enabled%>

max_attempts=60
cass_ip="<%= spec.ip %>"
cass_port="<%= p('cassandra_seed.native_transport_port') %>"
attempts=0
while ! nc -z "$cass_ip" "$cass_port"; do
    attempts=$(($attempts + 1))
    if [[ $attempts -ge $max_attempts ]]; then
        echo "ERROR: could not reach cassandra on IP '$cass_ip' and TCP port '$cass_port' after '$max_attempts' attemps. Aborting." >&2
        exit 1
    fi
    sleep 1
done
echo "INFO: reached Cassandra on '$cass_ip:$cass_port' after '$attempts' attemps." \
     "Waiting 30 more seconds for the service to be available." >&2
sleep 30

echo "converging password, attempt 1: use default password" >&2
/var/vcap/packages/cassandra/bin/cqlsh --cqlshrc "/var/vcap/jobs/cassandra_seed/root/.cassandra/cqlshrc" \
    -e "alter role cassandra with password = '$CASS_PWD' " -u cassandra -p cassandra
failure=$?
echo "attempt 1 exit status: '$failure'" >&2

if [[ "$failure" == 1 ]]; then
    echo "converging password, attempt 2: use new password" >&2
    /var/vcap/packages/cassandra/bin/cqlsh --cqlshrc "/var/vcap/jobs/cassandra_seed/root/.cassandra/cqlshrc" \
        -e "alter role cassandra with password = '$CASS_PWD' " -u cassandra -p "$CASS_PWD"
    echo "attempt 2 exit status: '$?'" >&2
else
	echo "Waiting 5 secs for the cassandra password to effectively be changed" >&2
	sleep 5
fi


/var/vcap/packages/cassandra/bin/cqlsh --cqlshrc "/var/vcap/jobs/cassandra_seed/root/.cassandra/cqlshrc" \
    -e "alter keyspace system_auth WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '3'}  AND durable_writes = true"

/var/vcap/jobs/cassandra_seed/bin/node-tool.sh repair system_auth

/var/vcap/jobs/cassandra_seed/bin/creer_pem_cli_serv.sh
exit 0
