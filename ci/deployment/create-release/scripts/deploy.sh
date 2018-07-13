#!/usr/bin/env bash 

set -ex

ROOT_FOLDER=${PWD}

export BOSH_CONFIG=$PWD/bosh-director-config/bosh_config.yml

deployment_ops_files="admin-tools.yml,use-bpm.yml"

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
				     cassandra-bosh-release/deployment/cassandra.yml \
                -o cassandra-bosh-release/deployment/operations/admin-tools.yml \
                -o cassandra-bosh-release/deployment/operations/use-bpm.yml \
                -v deployment_name=${DEPLOYMENT_NAME} \
                -v network_name=${NETWORK} \
                -v vm_type=small \
                -v persistent_disk_type=small
##              --recreate
				

popd