#!/usr/bin/env bash 

set -ex

ROOT_FOLDER=${PWD}

export BOSH_CONFIG=$PWD/bosh-director-config/bosh_config.yml

bosh -e ${ALIAS} -d ${DEPLOYMENT_NAME} -n -d ${DEPLOYMENT_NAME} delete-deployment
