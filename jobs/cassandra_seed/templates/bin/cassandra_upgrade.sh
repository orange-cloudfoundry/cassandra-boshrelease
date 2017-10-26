#!/bin/bash

set -e # exit immediately if a simple command exits with a non-zero status.
set -u # report the usage of uninitialized variables.

export LANG=en_US.UTF-8

export CASSANDRA_BIN=/var/vcap/packages/cassandra/bin
export CASSANDRA_CONF=/var/vcap/jobs/cassandra_seed/conf

export JAVA_HOME=/var/vcap/packages/openjdk
export PATH=$PATH:/var/vcap/packages/openjdk/bin

export CASSANDRA_CONF=/var/vcap/jobs/cassandra_seed/conf
export CASS_PWD="<%=properties.cassandra_seed.cass_pwd%>"

pushd /var/vcap/packages/cassandra/bin
exec chpst -u vcap:vcap /var/vcap/packages/cassandra/bin/nodetool drain
exec chpst -u vcap:vcap /var/vcap/packages/cassandra/bin/nodetool snapshot -t upgrade_$(date +"%Y-%m-%d-%H-%M-%S")
exec chpst -u vcap:vcap /var/vcap/packages/cassandra/bin/nodetool upgradesstables
rm -f /var/vcap/store/FLAG_UPGRADE_CASSANDRA
popd
exit 0

