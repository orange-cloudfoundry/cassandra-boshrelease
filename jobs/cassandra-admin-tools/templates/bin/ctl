#!/usr/bin/env bash

JOB_NAME=cassandra-admin-tools
JOB_DIR=/var/vcap/jobs/$JOB_NAME


if [[ -d ${JOB_DIR}/tools/bin/graph ]]; then
    rm -rf $JOB_DIR/tools/bin/graph
fi
mkdir -p $JOB_DIR/tools/bin/graph
chmod 777 $JOB_DIR/tools/bin/graph
chmod +x  $JOB_DIR/tools/bin/cassandra-stress.sh
