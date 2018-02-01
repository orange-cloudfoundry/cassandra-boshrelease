Cassandra BOSH release
======================

This BOSH Release allows you to easily roll-out and maintain Cassandra
clusters, with the power of [BOSH](https://bosh.io).

In the `deployment/` directory, you'll find BOSH 2.0 deployment manifests and
operations files. They cover basic deployment, Cloud Foundry integration with
service broker deployment & registration, and integration with the
[SHIELD](https://shieldproject.io/) backup solution (v7 and v8).


## Usage

Provided that you have a working Bosh director and that it is properly
targeted by your Bosh CLI:

```bash
git clone https://github.com/orange-cloudfoundry/cassandra-cf-service-boshrelease.git cassandra-boshrelease
cd cassandra-boshrelease

bosh create-release
bosh upload-release

>> depl-state.yml
chmod 600 depl-state.yml

bosh -d cassandra deploy deployment/cassandra.yml \
    -o deployment/operations/admin-tools.yml \
    --vars-store depl-state.yml
```

## Base deployment

The `cassandat.yml` base manifests describes a classical deployment with 3
seeds and optionally more servers. It provides two separate instance groups
for this, named `cassandra-seeds` and `cassandra-servers`.

Cassandra seeds are plain Cassandra nodes with a special role in the cluster:
They are meant to “explain” the cluster topology (how data is spread accross
nodes) to any new node that enters the cluster. From the perspective of a BOSH
Release maintainer, the only particularity they have is to be listed in the
`seed_provider:` section of the `cassandra.yaml` configuration file.


## Operation files

### `cf-service-broker.yml`

This operation file add a `cassandra-brokers` instance group, a `broker-smoke-
tests` errand job, and both `broker-registrar` and `broker-deregistrar` errand
jobs.

The cassandra broker is purely stateless. The `create-service` verb creates a
new keyspace and the `bind-service` verb creates a new user. The broker uses
naming convention on those keyspaces and users. Keyspaces typically start with
the `ks` prefix.

The Service Broker comes with it `broker-smoke-tests` errand job that
implement a full round-trip around pushing an app in Cloud Foundry, binding a
Cassandra service to it, writing some data, and checking it can be read back.

The `broker-registrar` and `broker-deregistrar` errand jobs are provided by
the standard
[`broker-registrar` BOSH release](https://github.com/cloudfoundry-community/broker-registrar-boshrelease),
as provided by the Cloud Foundry community. The you can register your broker
in CF with this command:

```
bosh -d cassandra run-errand broker-registrar
```

If you are not familiar with the semantics of this `broker-registrar` BOSH
release, you just need to be warned that the `broker-deregistrar` job actually
purges all services offerings from Cloud Foundry, which can be pretty
destructive for your Clooud Foundry users. This needs to be used with caution.

With this `cf-service-broker.yml` operation file, we assumes that you are
using the *de facto* standard `cf-admin-user` job from the
[`collection-of-pullrequests` BOSH Release](https://github.com/cloudfoundry-community/collection-of-pullrequests-boshrelease),
(as provided by the Cloud Foundry community), wich is pretty useful at
providing a BOSH 2.0 Link that transmits Cloud Foundry settings to the Broker
(De-)Registrar errand jobs.


### `shield-v7-agent.yml`

The `cassandra` SHIELD plugin was merged into v7 branch in the version
`v7.0.4` of the SHIELD Bosh Release.

The SHIELD auto-config uses a mini-templated syntax with idioms like `(ip)`
and `(deployment)`. This is due to the need for creating separate targets for
each Cassandra nodes. Indeed, when a given keyspace has a replication factor
that is less than the number of nodes in the cluster (which is the case for
the vast majority of cassandra deployment), then you need to backup *all*
cluster nodes at the same time if you want to snapshot all keyspace data.

In terms of consistency, you don't get strict ACID consistency with such a
design. Still, this is a working backup solution. And until there is something
better available you'll need it for your data-services deployments.


### `shield-v8-agent.yml`

This operation file provides pretty much the same as its v7 counterpart, but
here for SHIELD v8.

The `cassandra` SHIELD plugin was merged into v8 branch as of version
`v8.0.5` of the SHIELD Bosh Release.

A few variables need to be specified, like the `((shield_domain))`which is an
IP address or a DNS name that Cassandra nodes can use to reach the SHIELD
“core” server. The `((shield-ca))` certificate should be the one from your
SHIELD deployment. We recommend you upload it in your CredHub config server
first, or use an absolute-path syntax that will target it in CredHub:

```
((/<bosh-director-name>/<shield-deployment-name>/shield-ca.certificate))
```


### `admin-tool.yml`

This operation file adds a job that provides wrappers around usual cassandra
administrative tools. These are provided by this BOSH Release for human
convenience only, so they are definitely not required for a Cassandra cluster
to properly work. They are even not recommended in production, as they can
provide a larger attack surface to intruders.


### `bosh-lite.yml`

This operation file implements the necessary settings for deploying in Bosh-Lite.


## BOSH 1.0 manifests

Example BOSH 1.0 manifests can be found in th `manifests/` subdirectory. These
are mean to be examples and not be shipped in later version of this BOSH
Release, as BOSH 1.0 is being sunet.


## Backups

The SHIELD v7 and v8 `cassandra` plugins are designed to help you backup your
Cassandra cluster, one keyspace at a time.

Please ensure that the `/var/vcap/cassandra/job/bin` directory is added to the
SHIELD v8 `env.path` configuration property. Indeed, this SHIELD plugin relies
on some `nodetool` and `sstableloader` wrapper scripts that are provided in
that directory.

As a result of the backup strategy implemented by the SHIELD plugin, extra
space is required on the persistent disk. As a rule of the thumb, you should
provide twice the persistent storage required for your data.


### Log Files

There are several places that have log files related to cassandra.

- Cassandra Broker - The logs can be found at
  `/var/vcap/data/sys/log/broker/broker.log`. This will basically log all the
  service broker code about creation/deletion of service/keyspaces etc.

- Cassandra nodes - The logs can be found at
  `/var/vcap/data/sys/log/cassandra/cassandra.stdout.log`. This will basically
  log the cassandra server startup logs and is helpful in determing what went
  wrong during startup.

- Cassandra runtime log - The logs can be found at
  `/var/vcap/data/sys/log/cassandra/system.log` in the same directory where
  data/commitlog is present.


### Running Nodetool/CQL-sh/Cassandra-Stress

If you are SSH'ed into the cassandra VM and need to run the standard `cqlsh`,
`nodetool`, `sstableloader` utilities, wrapper scripts are provided by the
`cassandra-admin-tools` optional job for convenience.

```bash
cd /var/vcap/jobs/cassandra-admin-tools/bin
```

To run the `nodetool` utility, run this command:

```bash
./node-tool.sh
```

To run the `cqlsh` CLI, run this command:

```bash
./cql-sh.sh"
```


### Cleanup/Removing snapshot

When a keyspace is deleted/dropped cassandra takes a snapshot of the keyspace
for security/backup purpose. if you wish to remove the snapshot you will have
to do it manually by running this command:

```bash
cd /var/vcap/jobs/cassandra-admin-tools/bin
./node-tool.sh clearsnapshot
```
