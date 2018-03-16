#!/bin/bash
#set -x
set -e # exit immediately if a simple command exits with a non-zero status.
set -u # report the usage of uninitialized variables.
cd /var/vcap/jobs/cassandra/config/certs/
export JAVA_HOME=/var/vcap/packages/openjdk
export PATH=$PATH:$JAVA_HOME/bin:.

export HOST_NAME1=`hostname -I`
export HOST_NAME=${HOST_NAME1//[[:space:]]}
export STOREPASS="<%= p("keystore_password") %>"
export KEYPASS=$STOREPASS
keytool -genkeypair -keyalg RSA -alias ${HOST_NAME} -keystore ${HOST_NAME}.jks -storepass $STOREPASS -keypass $KEYPASS -validity 3650 -keysize 2048 -dname "CN=${HOST_NAME}, OU=TestCluster, O=Orange, L=Lyon, S=FR, C=FR"

keytool -keystore ${HOST_NAME}.jks -alias ${HOST_NAME} -certreq -file ${HOST_NAME}.csr -keypass $KEYPASS -storepass $STOREPASS
exit 0
