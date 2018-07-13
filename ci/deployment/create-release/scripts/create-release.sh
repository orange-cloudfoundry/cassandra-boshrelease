#!/usr/bin/env bash 

set -ex

ROOT_FOLDER=${PWD}

export BOSH_CONFIG=$PWD/bosh-director-config/bosh_config.yml

pushd cassandra-bosh-release|| exit 666

# Updating final.yml with release name specified in settings
sed -i -e "s/^\(final_name:\).*/\1 ${BOSH_RELEASE}/" config/final.yml

# removing deployment
bosh -e ${ALIAS} delete-deployment -n -d ${DEPLOYMENT_NAME}

# removing already existing release if exists
bosh -e ${ALIAS} releases | cat | grep ${BOSH_RELEASE} |while read rel ver other
do
	if [ "${rel}" == "${BOSH_RELEASE}" ]
	then	
		bosh -e ${ALIAS} -n delete-release ${rel}/${ver}
	fi
done



## bosh -e ${ALIAS} create-release --force --version ${CASSANDRA_VERSION}
bosh -e ${ALIAS} create-release --force 

bosh -e ${ALIAS} upload-release

popd

