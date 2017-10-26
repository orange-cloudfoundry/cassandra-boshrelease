#!/bin/bash

set -e # exit immediately if a simple command exits with a non-zero status.
set -u # report the usage of uninitialized variables.

ulimit -n 131072

# Setup env vars and folders for the webapp_ctl script
source /var/vcap/jobs/cassandra_seed/helpers/ctl_setup.sh 'cassandra_seed'

export LANG=en_US.UTF-8

export CASSANDRA_BIN=/var/vcap/packages/cassandra/bin
export CASSANDRA_CONF=/var/vcap/jobs/cassandra_seed/conf

export JAVA_HOME=/var/vcap/packages/openjdk
export PATH=$PATH:/var/vcap/packages/openjdk/bin

export CASS_UPG=<%=properties.cassandra_seed.cass_upgrade_TF%>

case $1 in

  start)

    if [[ ${CASS_UPG} == "true" ]]
    then
       touch /var/vcap/store/FLAG_UPGRADE_CASSANDRA >>$LOG_DIR/cassandra.stdout.log 2>>$LOG_DIR/cassandra.stderr.log
    fi

    pid_guard $PIDFILE $JOB_NAME

    exec chpst -u vcap:vcap $CASSANDRA_BIN/cassandra -p $PIDFILE \
    >>$LOG_DIR/cassandra.stdout.log \
    2>>$LOG_DIR/cassandra.stderr.log

    ;;

  stop)
    kill_and_wait $PIDFILE

    ;;
  *)
    echo "Usage: cassandra_seed.ctl {start|stop}"

    ;;

esac

exit 0
