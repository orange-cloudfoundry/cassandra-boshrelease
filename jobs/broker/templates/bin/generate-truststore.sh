#!/usr/bin/env bash
<%
  require "shellwords"

  def esc(x)
      Shellwords.shellescape(x)
  end
%>

set -e # exit immediately if a simple command exits with a non-zero status
set -u # report the usage of uninitialized variables

CERTS_DIR=/var/vcap/jobs/broker/tls
truststore_password=<%= esc(p("broker.truststore_password")) %>

date +%F_%T
/var/vcap/packages/openjdk/bin/keytool -import -v \
    -trustcacerts \
    -alias cassandra-ca \
    -file "$CERTS_DIR/cassandra_ca.crt" \
    -keystore "$CERTS_DIR/cassandra_truststore.jks" \
    -storepass "$truststore_password" \
    -keypass "$truststore_password" \
    -noprompt

chown vcap:vcap "$CERTS_DIR/cassandra_truststore.jks"
chmod 600 "$CERTS_DIR/cassandra_truststore.jks"
