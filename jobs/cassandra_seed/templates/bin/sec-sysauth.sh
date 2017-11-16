#!/bin/bash

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

if ${CLIENT_SSL} == "true"
then
 /var/vcap/packages/cassandra/bin/cqlsh --cqlshrc "/var/vcap/jobs/cassandra_seed/root/.cassandra/cqlshrc"  -e "alter role cassandra with password = '$CASS_PWD' " -u cassandra -p cassandra --ssl 2>&1>/dev/null
 if [[ "$?" == 1 ]]; then
  /var/vcap/packages/cassandra/bin/cqlsh --cqlshrc "/var/vcap/jobs/cassandra_seed/root/.cassandra/cqlshrc"  -e "alter role cassandra with password = '$CASS_PWD' " -u cassandra -p $CASS_PWD --ssl 2>&1>/dev/null
 fi
 /var/vcap/packages/cassandra/bin/cqlsh --cqlshrc "/var/vcap/jobs/cassandra_seed/root/.cassandra/cqlshrc"  -e "alter keyspace system_auth WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '3'}  AND durable_writes = true" -u cassandra -p $CASS_PWD --ssl  2>&1>/dev/null
 /var/vcap/jobs/cassandra_seed/bin/./node-tool.sh repair system_auth
#exit 0
else
	echo "converging password, attempt 1: use default password" >&2
    /var/vcap/packages/cassandra/bin/cqlsh `hostname -I` -e "alter role cassandra with password = '$CASS_PWD' " -u cassandra -p cassandra
    if [[ "$?" == 1 ]]; then
		echo "converging password, attempt 2: use new password" >&2
        /var/vcap/packages/cassandra/bin/cqlsh `hostname -I` -e "alter role cassandra with password = '$CASS_PWD' " -u cassandra -p $CASS_PWD
    fi
    /var/vcap/packages/cassandra/bin/cqlsh `hostname -I` -e "alter keyspace system_auth WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '3'}  AND durable_writes = true" -u cassandra -p $CASS_PWD
    /var/vcap/jobs/cassandra_seed/bin/./node-tool.sh repair system_auth
fi

/var/vcap/jobs/cassandra_seed/bin/./creer_pem_cli_serv.sh
exit 0
