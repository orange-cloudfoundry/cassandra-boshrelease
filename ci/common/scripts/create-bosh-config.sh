#!/usr/bin/env bash 

set -ex

mkdir -p bosh-director-config
cd bosh-director-config || exit 115

# retrieving certificate provided by pipeline
cat > ./bosh_ca.crt <<EOF
$CA_CERT
EOF

export BOSH_CONFIG=$PWD/bosh_config.yml

bosh -e $IP alias-env $ALIAS --ca-cert=./bosh_ca.crt
( echo $USER ; echo $PASSWORD ) \
| bosh -e $ALIAS log-in
