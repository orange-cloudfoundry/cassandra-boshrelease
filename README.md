# Cassandra CF Service

## Introduction

This page describes the architecture of Cassandra service for CloudFoundry
using the new Service Broker API version 2.

## Components

### Casssandra Broker

The cassandra broker implements the 5 REST endpoints required by Cloud Foundry
to write V2 services. Cassandra Broker is divided into 3 components.

* The Broker code itself that implements the 5 REST endpoints
* Cassandra Admin Service which uses Cassandra DataStax client to connect to
  runnning cassandra cluster and create/deletes keyspace
* H2 Database which saves service related meta deta (For eg. service id,
  credentials, # of services)

### Cassandra Server

The Cassandra server (node) is deployed on a seperate VM and can be deployed
on any number of VMs depending on how many nodes we want as part of the
cluster. The Cassandra admin service mentioned above creates keySpace on this
running Cassandra Cluster for further consumption.

## How to deploy

We use BOSH to deploy Cassandra Broker and Cassandra Nodes (Running Cassandra
server). Both Broker and Cassandra are intergrated with monit which will
restart broker and Cassandra Process in case of VM is restarted or process in
crashed.

* `bosh create release --force` (from the parent directory)
* Upload the release to the Bosh Director (`bosh upload release`)
* Deploy the release using manifest file (Sample manifest file can be found
  in `cassandra_broker.yml`)

## Configuring CF to use Cassandra service

### Authentication

According to [Managing Service Brokers](https://docs.pivotal.io/pivotalcf/services/managing-service-brokers.html)
brokers should use HTTP basic authentication to authenticate clients. The
`cf create-service-broker` command expects the credentials for the cloud
controller to authenticate itself to the broker. This is set in the broker in
the `config-context.xml` file, the values in p4 for testing are
admin/password.

Use `cf add-service-broker` command to add the cassandra broker for consumption.

```bash
cf create-service-broker
name> anyName
URL> http://<IP of broker deployed via bosh>:8080
It will prompt for username and password use admin/password
```

After the broker is added to CF the service plan needs to be made public. You
can read here on how to make service plans public. See
[Access Control](http://docs.pivotal.io/pivotalcf/services/access-control.html)
for more information.

### Available Plans

There are 2 Plans available for Cassandra.

* Developer - This plan deletes the cassandra keySpace when service is deleted
* Production - This plan won't delete any data even if service is deleted

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
  `/var/vcap/sys/log/cassandra_broker`. This will basically log all the
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

## Cleanup/ Removing snapshot

When a keyspace is deleted/dropped cassandra takes a snapshot of the keyspace
for security/backup purpose. if you wish to remove the snapshot you will have
to do it manually by running this command:

```sh
cd /var/vcap/packages/cassandra_server/bin (or cd /var/vcap/packages/cassandra_seed/bin or cd /var/vcap/packages/cassandra_injector repectively if you are ou a cassandra server, seed or injector node)
./nodetool clearsnapshot  
```

##Future Enhancements

## You can now run any arguments with all the binaries below (nodetool, cassandra-stress) 
## and also cql-sh with all kind of possible arguments

```sh
cd /var/vcap/packages/cassandra_server/bin # (or cd /var/vcap/packages/cassandra_seed/bin or cd /var/vcap/packages/cassandra_injector repectively if you are ou a cassandra server, seed or injector node)
./nodetool status -r # (to obtain all the hostname of your cluster and to use one or more of it to connect with)
```

### Authorization

Currently the Cassandra keySpace created via service can be used by any user
if they have the Cassandra nodes IP address. In future this can be avoided by
creating authorization per keySpace and return back credential to access that
keySpace only to the app/user who consumes that service.
