#!/usr/bin/env bash
<%
  require "shellwords"

  def esc(x)
      Shellwords.shellescape(x)
  end
%>

set -e -o pipefail # If a command fails, in a pipeline or not, exit immediately
set -u # report the usage of uninitialized variables

CERTS_DIR=/var/vcap/jobs/cassandra/tls
readonly CERTS_DIR
KEYSTORE_PASSWORD=<%= esc(p("keystore_password", "")) %>
readonly KEYSTORE_PASSWORD

function restrict_access_to() {
    local file=$1
    chown vcap:vcap "$file"
    chmod 600 "$file"
}

function import_in_keystore_ca_and_cert_and_key() {
    local ca_file=$1
    local cert_file=$2
    local key_file=$3

    openssl pkcs12 -export \
        -in "$cert_file" \
        -inkey "$key_file" \
        -name cassandra-node \
        \
        -CAfile "$ca_file" \
        -caname cassandra-ca \
        \
        -out "$CERTS_DIR/tmp-keystore.p12" \
        -passout "pass:$KEYSTORE_PASSWORD"

    /var/vcap/packages/openjdk/bin/keytool -importkeystore -v \
        -srckeystore "$CERTS_DIR/tmp-keystore.p12" \
        -srcstoretype PKCS12 \
        -srcstorepass "$KEYSTORE_PASSWORD" \
        -alias cassandra-node \
        \
        -deststorepass "$KEYSTORE_PASSWORD" \
        -destkeypass "$KEYSTORE_PASSWORD" \
        -destkeystore "$CERTS_DIR/cassandra_keystore.jks"

    rm "$CERTS_DIR/tmp-keystore.p12"
}

function import_in_truststore_cert_as_alias() {
    local cert_file=$1
    local cert_alias=$2

    /var/vcap/packages/openjdk/bin/keytool -importcert -v \
        -trustcacerts \
        -alias "$cert_alias" \
        -file "$cert_file" \
        -keystore "$CERTS_DIR/cassandra_truststore.jks" \
        -storepass "$KEYSTORE_PASSWORD" \
        -keypass "$KEYSTORE_PASSWORD" \
        -noprompt
}

rm -f "$CERTS_DIR/cassandra_keystore.jks"
rm -f "$CERTS_DIR/cassandra_truststore.jks"

import_in_keystore_ca_and_cert_and_key \
    "$CERTS_DIR/ca.crt" \
    "$CERTS_DIR/node.crt" \
    "$CERTS_DIR/node.key"

restrict_access_to "$CERTS_DIR/cassandra_keystore.jks"

<%
    if_p('trusted_ca_certs') do |trusted_ca_certs|
        if trusted_ca_certs.length == 0
            # We at least import the node certificate CA
%>
import_in_truststore_cert_as_alias "$CERTS_DIR/ca.crt" "cassandra-ca"
<%
        else
            trusted_ca_certs.each_index do |idx|
%>
# import trusted certificate #<%= idx %>
ca_cert_file=$CERTS_DIR/ca.<%= esc(idx) %>.crt
cat > "$ca_cert_file" <<END_OF_CERTIFICATE
<%= trusted_ca_certs[idx] %>
END_OF_CERTIFICATE
import_in_truststore_cert_as_alias "$ca_cert_file" cassandra-ca-<%= esc(idx) %>
<%
            end
        end
    end
-%>

restrict_access_to "$CERTS_DIR/cassandra_truststore.jks"
