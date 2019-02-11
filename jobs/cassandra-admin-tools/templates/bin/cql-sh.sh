#!/usr/bin/env bash

set -e # exit immediately if a simple command exits with a non-zero status.
set -u # report the usage of uninitialized variables.

export LANG=en_US.UTF-8
export PATH=/var/vcap/packages/python/usr/bin:$PATH
exec chpst -u vcap:vcap \
    env HOME=/home/vcap \
        /var/vcap/packages/cassandra/bin/cqlsh \
            --cqlshrc /var/vcap/jobs/cassandra-admin-tools/root/.cassandra/cqlshrc \
            "$@"
