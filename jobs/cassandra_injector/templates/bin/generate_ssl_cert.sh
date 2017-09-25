#!/bin/bash

set -e # exit immediately if a simple command exits with a non-zero status.
set -u # report the usage of uninitialized variables.

#source `dirname $(readlink --canonicalize-existing $0)`/setenv

pushd `dirname $(readlink --canonicalize-existing $0)` >/dev/null
export CASSANDRA_SSL=/var/vcap/jobs/cassandra_injector/ssl
cd ${CASSANDRA_SSL}

#[ -f server.pem ] && rm -f server.pem 
#[ -f client.pem ] && rm -f client.pem

touch .rnd
export RANDFILE="${CASSANDRA_SSL}"/.rnd

# generate only if client and server keys doesnt already exists
if [ ! -f server.pem -o ! -f client.pem ]
then
	node_current_ip=`hostname -I`
	perl -e 'my $n1=int(rand(10));my $n2=int(rand(10));print "$n1$n2\n"' > cassandradb.srl # two random digits number
	openssl genrsa -out server.key 2048
	openssl req -key server.key -new -out server.req -subj  "/C=FR/ST=IDF/O=Cloud Foundry/CN=${node_current_ip}/emailAddress=user@domain.com"
	openssl x509 -req -in server.req -CA cassandradb.ca -CAkey cassandradb.pem -CAserial cassandradb.srl -out server.crt -days 3650
	cat server.key server.crt > server.pem

	openssl genrsa -out client.key 2048
	openssl req -key client.key -new -out client.req -subj "/C=FR/ST=IDF/O=Cloud Foundry/CN=${node_current_ip}/emailAddress=user@domain.com"
	openssl x509 -req -in client.req -CA cassandradb.ca -CAkey cassandra.pem -CAserial cassandradb.srl -out client.crt -days 3650
	cat client.key client.crt > client.pem
	#openssl verify -CAfile cassandradb.ca client.pem

	# removing unneeded files
	rm *.req *.srl *.crt *.key .rnd
fi

popd >/dev/null
exit 0
