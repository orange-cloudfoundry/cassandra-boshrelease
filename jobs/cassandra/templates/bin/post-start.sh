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

job_dir=/var/vcap/jobs/cassandra

export CASSANDRA_BIN=/var/vcap/packages/cassandra/bin
export CASSANDRA_CONF=$job_dir/conf

export JAVA_HOME=/var/vcap/packages/openjdk
export PATH=$PATH:$JAVA_HOME/bin:$CASSANDRA_BIN


function log_err() {
    echo "$(date +%F_%T):" "$@" >&2
}


max_attempts=120
cass_ip=<%= esc(spec.ip) %>
cass_port=<%= esc(p('native_transport_port')) %>
attempts=0
while ! nc -z "$cass_ip" "$cass_port"; do
    attempts=$(($attempts + 1))
    if [[ $attempts -ge $max_attempts ]]; then
        log_err "ERROR: could not reach cassandra on IP '$cass_ip' and TCP port '$cass_port'" \
             "after '$max_attempts' attemps. Aborting."
        exit 1
    fi
    sleep 1
done
log_err "INFO: reached Cassandra on '$cass_ip:$cass_port' after '$attempts' attemps." \
     "Waiting 30 more seconds for the service to be available."
sleep 30



password_file=/var/vcap/store/cassandra/cassandra.secret
readonly password_file
function store_password() {
    local password=$1
    printf "$password" | base64 > "$password_file"
}

if [ ! -e "$password_file" ]; then
    store_password cassandra
fi
chown root:vcap "$password_file"
chmod 600 "$password_file"
cassandra_password=$(cat "$password_file" | base64 --decode)
new_cassandra_password=<%= esc(p('cassandra_password')) %>

log_err "INFO: setting password"
$CASSANDRA_BIN/cqlsh --cqlshrc "$job_dir/root/.cassandra/cqlshrc" \
    -u cassandra -p "$cassandra_password" \
    -e "alter role cassandra with password = '$new_cassandra_password'"
failure=$?
log_err "DEBUG: setting password, exit status: '$failure'"
if [ "$failure" != 0 ]; then
    log_err "ERROR: the password for user 'cassandra' is inconsistent. Aborting."
    exit 1
fi

store_password "$new_cassandra_password"

log_err "INFO: waiting 5 secs for the cassandra password to effectively be changed"
sleep 5



log_err "INFO: setting replication strategy for cassandra password"
<%
  require "json"

  replication_factor = p('system_auth_keyspace_replication_factor', link('seeds').instances.count)
-%>
# Note: should we support multiple datacenters one day, then we should set the
# replication class to 'NetworkTopologyStrategy' here instead of 'SimpleStrategy'.
# See: <https://docs.datastax.com/en/cassandra/latest/cassandra/configuration/configCassandra_yaml.html#configCassandra_yaml__authenticator>
# See: <https://docs.datastax.com/en/cassandra/latest/cassandra/configuration/configCassandra_yaml.html#configCassandra_yaml__authorizer>
$CASSANDRA_BIN/cqlsh --cqlshrc "$job_dir/root/.cassandra/cqlshrc" \
     -e 'alter keyspace system_auth WITH replication = {"class": "SimpleStrategy", "replication_factor": <%= replication_factor.to_json %>}  AND durable_writes = true'

log_err "INFO: propagating any new password with the enforced replication strategy"
$job_dir/bin/nodetool repair system_auth

log_err "INFO: creating SSL certificates"
$job_dir/bin/creer_pem_cli_serv.sh

exit 0
