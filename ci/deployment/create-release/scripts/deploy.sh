#!/usr/bin/env bash 

set -ex

ROOT_FOLDER=${PWD}

export BOSH_CONFIG=$PWD/bosh-director-config/bosh_config.yml

deployment_ops_files="admin-tools.yml,use-bpm.yml"

# Updating final.yml with release name specified in settings
sed -i -e "s/^\(final_name:\).*/\1 ${BOSH_RELEASE}/" ../../../config/final.yml


# RELEASE_VERSION=$(grep '^cassandra' ${ROOT_FOLDER}/versions/keyval.properties \
#                | cut -d'=' -f2)

deployment_var="  	-v deployment=${DEPLOYMENT_NAME} \
						-v release=${BOSH_RELEASE} \
                        -v release_version=${RELEASE_VERSION} \
    					-v instance_group=${INSTANCE_GROUP} \
    					-v network=${NETWORK} \
                        -v director_uuid=${UUID}"
#    					-v director_uuid=${UUID} \
#    					-v version=${RELEASE_VERSION}"


bosh -e ${ALIAS} -d ${DEPLOYMENT_NAME} -n deploy \
				     ${PWD}/../tasks/cassandra.yml \
                -o ${PWD}/../tasks/admin-tools.yml \
                -o ${PWD}/../tasks/use-bpm.yml \
                -var deployment_name=${DEPLOYMENT_NAME} 
##              --recreate
				
popd

mkdir -p deployed

pushd deployed || exit 666
touch keyval.properties
popd