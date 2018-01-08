# <p style="text-align:center">Cassandra Bosh Release</p>

> > ** THIS RELEASE IS STILL WIP AND SHOULD NOT BE USE IN PRODUCTION **

## Table of contents

* [Introduction](#introduction)
* [Components](#components)
* [How to deploy](#how_to_deploy)
* [Configuring CF to use Cassandra service](#configuring_cf_to_use_cassandra_service)
* [Upgrades](#upgrades)
* [Future enhancements](#future_enhancements)

## Introduction

This page describes the architecture of Cassandra service for CloudFoundry using the new Service Broker API version 2.

## Components

### Casssandra Broker (broker job)

The cassandra broker implements the 5 REST endpoints required by Cloud Foundry to write V2 services : 
* Catalog management in order to register the broker to the platform
* Provisioning in order to create resource in the cassandra server
* Deprovisioning in order to release resource previously allocated
* Binding (credentials type) in order to provide application with a set of information required to use the allocated service
* Unbinding in order to delete credentials resources previously allocated
  
Cassandra Broker uses the Cassandra DataStax driver to connect to running cassandra cluster and gives order to the backend by using CQL statements

Current implementation is stateless which means no database (no requirement) 

### Casssandra Broker Smoke Tests (broker-smoke-tests job)

The cassandra broker smoke test acts as an end user developper who wants to host its application in a cloud foundry.

For that, it relies on a sample cassandra application : https://github.com/JCL38-ORANGE/cf-cassandra-example-app

The following steps are performed by the smoke tests job : 
* Authentication on Cloud Foundry by targeting org and space (cf auth and cf target)
* Deployment of the sample cassandra application (cf push)
* Provisioning of the service (cf create-service)
* Binding of the service (cf bind-service)
* Restaging of the sample cassandra application (cf restage)
* Table creation in the cassandra cluster (HTTP POST command to the sample cassandra application)
* Table deletion in the cassandra cluster (HTTP DELETE command to the sample cassandra application)

### Cassandra Server

The Cassandra server (node) is deployed on a seperate VM and can be deployed
on any number of VMs depending on how many nodes we want as part of the
cluster. The Cassandra admin service mentioned above creates keySpace on this
running Cassandra Cluster for further consumption.

## How to deploy

We use BOSH to deploy Cassandra Broker (and smoke tests) and Cassandra Nodes (Running Cassandra
server). Both Broker and Cassandra are integrated with monit which will
restart broker and Cassandra Process in case of VM is restarted or process in
crashed.

* `bosh create release --force` (from the parent directory)
* Upload the release to the Bosh Director (`bosh upload release`)
* Deploy the release using manifest file (Sample manifest file can be found
  in `cassandra_broker.yml`)

## Configuring CF to use Cassandra service

### Available Plans

For the moment, only 1 default plan available for shared Cassandra.

### Broker registration

The broker uses HTTP basic authentication to authenticate clients. The `cf create-service-broker` command expects the credentials for the cloud
controller to authenticate itself to the broker. 

```bash
cf create-service-broker p-cassandra-broker <user> <password> <url> 
cf enable-service-access cassandra
```

### Service provisioning

```bash
cf create-service cassandra default cassandra-instance
```

### Service binding

```bash
cf bind-service cassandra-example-app cassandra-instance
```
### Service unbinding

```bash
cf unbind-service cassandra-example-app cassandra-instance
```
### Service deprovisioning

```bash
cf delete-service cassandra-instance
```

## Cassandra Cluster Layout

The Cassandra Cluster layout and deployment is highly configurable and creates
a keySpace using `NetworktopologyStrategy`.

### Snitch

We use Property file Snitch for Cassandra to support multiple data center in
future which is populated via topology properties segment mentioned in the
next section.

### Configuring Cassandra Configuration

Sample Cassandra properties via manifest.

```yaml
properties:
  cassandra_server:
     cluster_name: <%= cassandra_cluster_name %>
     num_tokens: 256
     internode_encryption: none
     client_server_encryption: false
     seeds: 10.8.5.x,10.8.5.y,10.8.5.z
     persistent_directory: /var/vcap/store/cassandra_server
     max_heap_size: 4G
     heap_newsize: 800M
     topology:
       - 10.8.5.x=DC1:RAC1
       - 10.8.5.y=DC1:RAC1
       - 10.8.5.z=DC1:RAC1
       - 10.8.5.a=DC1:RAC1
       - 10.8.5.b=DC1:RAC1 
```      

The Cassandra properties files are made configurable via BOSH erb files and
are available in directory `config/`.

The Data center layout is configurable and can be configured via deployment
manifest file via the Topology section mentioned above.

## Backups

The SHIELD v8 `cassandra` plugin is designed to help you backup your Cassandra
cluster, one keyspace at a time.

This SHIELD plugin relies on some `nodetool` and `sstableloader` wrapper
scripts that will run the regular `nodetool` and `sstableloader` utilities
without requiring any environment variable to be provided (like JAVA_HOME or
CASSANDRA_CONF). Here in this BOSH release, these scripts are provided in
`/var/vcap/cassandra/job/bin`. Please ensure that this directory is added to
the SHIELD v8 `env.path` configuration property.

As a result of the backup strategy implemented by the SHIELD plugin, extra
space is required on the persistent disk. As a rule of the thumb, you should
provide twice the persistent storage required for your data.

## Upgrades

Upgrading Cassandra with a new Cassandra version or with some changes in the
properties is quite straightforward.

Since the deployment of the Cassandra Cluster is controlled via BOSH, we
leverage BOSH functionality to do any version/property upgrade.

### Upgrading Casandra Version

* Replace the Blob with the new version in the location `blobs/cassandra/`
* In the packaging script change the version name, scripts available at
  `packages/cassandra/`
* create release and deploy

### Upgrading/Updating Cassandra YAML

* Change the properties file present here `jobs/cassandra_server/templates/config/`
* There are certain values which are configurable via manifest file itself and
  mentioned in the above section.
* Create release and deploy

## Debugging

Bosh VMs will list out the IP address for Cassandra VMs. Cassandra data is
store in persistent directory `/var/vcap/store/cassandra_server`.

### Log Files

There are 4 places that have log files related to cassandra.

* Cassandra Broker - The logs can be found at
  `/var/vcap/sys/log/broker`. This will basically log all the
  service broker code about creation/deletion of service/keyspaces etc.
* Cassandra Server - The logs can be found at
  `/var/vcap/sys/log/cassandra_server`. This will basically log the cassandra
  server startup logs and is helpful in determing what went wrong during
  startup.
* Cassandra Seed - The logs can be found at `/var/vcap/sys/log/cassandra_seed`.
  This will basically log the cassandra server startup logs and is helpful in
  determing what went wrong during startup.
* Cassandra runtime log - The logs can be found at
  `/var/vcap/store/cassandra_server/system.log` in the same directory where
  data/commitlog is present.

### Running Nodetool/Cassandra-cli/Cassandra-Stress

If you are SSH'ed into the cassandra VM and need to run the nodetool or
cassandra-cli provided out of the box from cassandra, run this command from
with the Casasndra VM.

```bash
cd /var/vcap/packages/cassandra/bin
```

To run nodetool run this command:

```bash
"JAVA_HOME=/var/vcap/packages/java/jre1.7.0_55 CASSANDRA_CONF=/var/vcap/jobs/cassandra_server/conf ./nodetool status"
```

To run cassandra-cli run this command:

```bash
"JAVA_HOME=/var/vcap/packages/java/jre1.7.0_55 CASSANDRA_CONF=/var/vcap/jobs/cassandra_server/conf ./cassandra-cli"
```

### Cleanup/ Removing snapshot

When a keyspace is deleted/dropped cassandra takes a snapshot of the keyspace
for security/backup purpose. if you wish to remove the snapshot you will have
to do it manually by running this command:

```sh
cd /var/vcap/packages/cassandra_server/bin (or cd /var/vcap/packages/cassandra_seed/bin or cd /var/vcap/packages/cassandra_injector repectively if you are ou a cassandra server, seed or injector node)
./nodetool clearsnapshot  
```

## Future Enhancements

You can now run any arguments with all the binaries below (nodetool,
cassandra-stress) and also cql-sh with all kind of possible arguments.

```sh
cd /var/vcap/packages/cassandra_server/bin # (or cd /var/vcap/packages/cassandra_seed/bin or cd /var/vcap/packages/cassandra_injector repectively if you are ou a cassandra server, seed or injector node)
./nodetool status -r # (to obtain all the hostname of your cluster and to use one or more of it to connect with)
```

### Authorization

Currently the Cassandra keySpace created via service can be used by any user
if they have the Cassandra nodes IP address. In future this can be avoided by
creating authorization per keySpace and return back credential to access that
keySpace only to the app/user who consumes that service.
