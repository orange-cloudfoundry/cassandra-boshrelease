#!/bin/bash
#set -x
set -e # exit immediately if a simple command exits with a non-zero status.
set -u # report the usage of uninitialized variables.
export KEY_STORE_PATH=/var/vcap/jobs/cassandra_server/config/certs/
cd ${KEY_STORE_PATH}
export JAVA_HOME=/var/vcap/packages/openjdk
export PATH=$PATH:$JAVA_HOME/bin:.

export HOST_NAME1=`hostname -I`
export HOST_NAME=${HOST_NAME1//[[:space:]]}

export KEY_STORE="$KEY_STORE_PATH/${HOST_NAME}_cassandra.keystore"
export PKS_KEY_STORE="$KEY_STORE_PATH/${HOST_NAME}_cassandra.pks12.keystore"
export TRUST_STORE="$KEY_STORE_PATH/${HOST_NAME}_cassandra.truststore"
export CLUSTER_NAME="<%=properties.cassandra_server.cluster_name%>"
export CLUSTER_PUBLIC_CERT="$KEY_STORE_PATH/${HOST_NAME}_CLUSTER_${CLUSTER_NAME}_PUBLIC.cer"
export CLIENT_PUBLIC_CERT="$KEY_STORE_PATH/${HOST_NAME}_CLIENT_${CLUSTER_NAME}_PUBLIC.cer"

export STOREPASS="<%=properties.cassandra_server.cass_KSP%>"
export KEYPASS=$STOREPASS
export PASSWORD=$KEYPASS


### Cluster key setup.
# Create the cluster key for cluster communication.
keytool -genkey -keyalg RSA -alias "${HOST_NAME}_${CLUSTER_NAME}_CLUSTER" -keystore "$KEY_STORE" -storepass "$PASSWORD" -keypass "$PASSWORD" \
-dname "CN=${HOST_NAME} $CLUSTER_NAME cluster, OU=Orange, O=Orange, L=Lyon, C=FR" \
-validity 36500

# Create the public key for the cluster which is used to identify nodes.
keytool -export -alias "${HOST_NAME}_${CLUSTER_NAME}_CLUSTER" -file "$CLUSTER_PUBLIC_CERT" -keystore "$KEY_STORE" \
-storepass "$PASSWORD" -keypass "$PASSWORD" -noprompt

# Import the identity of the cluster public cluster key into the trust store so that nodes can identify each other.
keytool -import -v -trustcacerts -alias "${HOST_NAME}_${CLUSTER_NAME}_CLUSTER" -file "$CLUSTER_PUBLIC_CERT" -keystore "$TRUST_STORE" \
-storepass "$PASSWORD" -keypass "$PASSWORD" -noprompt


### Client key setup.
# Create the client key for CQL.
keytool -genkey -keyalg RSA -alias "${HOST_NAME}_${CLUSTER_NAME}_CLIENT" -keystore "$KEY_STORE" -storepass "$PASSWORD" -keypass "$PASSWORD" \
-dname "CN=${HOST_NAME} $CLUSTER_NAME cluster, OU=Orange, O=Orange, L=Lyon, C=FR" \
-validity 36500

# Create the public key for the client to identify itself.
keytool -export -alias "${HOST_NAME}_${CLUSTER_NAME}_CLIENT" -file "$CLIENT_PUBLIC_CERT" -keystore "$KEY_STORE" \
-storepass "$PASSWORD" -keypass "$PASSWORD" -noprompt

# Import the identity of the client pub  key into the trust store so nodes can identify this client.
keytool -importcert -v -trustcacerts -alias "${HOST_NAME}_${CLUSTER_NAME}_CLIENT" -file "$CLIENT_PUBLIC_CERT" -keystore "$TRUST_STORE" \
-storepass "$PASSWORD" -keypass "$PASSWORD" -noprompt

keytool -importkeystore -srckeystore "$KEY_STORE" -destkeystore "$PKS_KEY_STORE" -deststoretype PKCS12 \
-srcstorepass "$PASSWORD" -deststorepass "$PASSWORD"

openssl pkcs12 -in "$PKS_KEY_STORE" -nokeys -out "${HOST_NAME}_${CLUSTER_NAME}_CLIENT.cer.pem" -passin pass:"$PASSWORD"
openssl pkcs12 -in "$PKS_KEY_STORE" -nodes -nocerts -out "${HOST_NAME}_${CLUSTER_NAME}_CLIENT.key.pem" -passin pass:"$PASSWORD"

exit 0
