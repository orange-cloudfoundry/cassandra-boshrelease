#!/usr/bin/env bash

<%
  require "shellwords"

  def esc(x)
      Shellwords.shellescape(x)
  end
%>

# set -x # Print commands and their arguments as they are executed.
set -u # report the usage of uninitialized variables.

export LANG=en_US.UTF-8

job_dir=/var/vcap/jobs/cassandra_injector

export CASSANDRA_BIN=/var/vcap/packages/cassandra/bin
export CASSANDRA_CONF=$job_dir/conf

export JAVA_HOME=/var/vcap/packages/openjdk
export PATH=$PATH:$JAVA_HOME/bin:$CASSANDRA_BIN

cass_pwd=<%= esc(p('cassandra_injector.cass_pwd')) %>


max_attempts=60
cass_ip=<%= esc(spec.ip) %>
cass_port=<%= esc(p('cassandra_injector.native_transport_port')) %>
attempts=0
while ! nc -z "$cass_ip" "$cass_port"; do
    attempts=$(($attempts + 1))
    if [[ $attempts -ge $max_attempts ]]; then
        echo "ERROR: could not reach cassandra on IP '$cass_ip' and TCP port '$cass_port'" \
			 "after '$max_attempts' attemps. Aborting." >&2
        exit 1
    fi
    sleep 1
done
echo "INFO: reached Cassandra on '$cass_ip:$cass_port' after '$attempts' attemps." \
     "Waiting 30 more seconds for the service to be available." >&2
sleep 30


echo "INFO: setting first password" >&2
$CASSANDRA_BIN/cqlsh --cqlshrc "$job_dir/root/.cassandra/cqlshrc" \
    -e "alter role cassandra with password = '$cass_pwd' " -u cassandra -p cassandra
failure=$?
echo "DEBUG: setting first password, exit status: '$failure'" >&2

if [[ "$failure" != 0 ]]; then
    echo "INFO: verifying that the current password is the desired password" >&2
    $CASSANDRA_BIN/cqlsh --cqlshrc "$job_dir/root/.cassandra/cqlshrc" \
        -e "alter role cassandra with password = '$cass_pwd' "
    failure2=$?
    echo "DEBUG: verifying current password, exit status: '$failure2'" >&2
	if [ "$failure2" != 0 ]; then
		echo "ERROR: the password for user 'cassandra' is inconsistent. Aborting." >&2
		exit 1
	fi
else
	echo "INFO: waiting 5 secs for the cassandra password to effectively be changed" >&2
	sleep 5
fi


echo "INFO: setting replication strategy for cassandra password" >&2
$CASSANDRA_BIN/cqlsh --cqlshrc "$job_dir/root/.cassandra/cqlshrc" \
     -e "alter keyspace system_auth WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '3'}  AND durable_writes = true"

echo "INFO: propagating any new password with the enforced replication strategy" >&2
$job_dir/bin/node-tool.sh repair system_auth

echo "INFO: creating SSL certificates" >&2
$job_dir/bin/creer_pem_cli_serv.sh

exit 0
