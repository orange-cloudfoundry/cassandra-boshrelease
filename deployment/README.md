# Base deployment

The `cassandra.yml` base manifests describes a classical deployment with 3
seeds and optionally more servers. It provides two separate instance groups
for this, named `cassandra-seeds` and `cassandra-servers`.

Cassandra seeds are plain Cassandra nodes with a special role in the cluster:
They are meant to “explain” the cluster topology (how data is spread accross
nodes) to any new node that enters the cluster. From the perspective of a BOSH
Release maintainer, the only particularity they have is to be listed in the
`seed_provider:` section of the `cassandra.yaml` configuration file.

Please note that you cannot set a number of seeds that is less than 3. This is
due to the replication factor for the `system` keyspace is currently hardwired
to `3`. As Cassandra seeds are mandatory, this implies that this Bosh Release
cannot deploy clusters with less than 3 Cassandra nodes.


# Operations files

## `cf-service-broker.yml`

This operations file add a `cassandra-brokers` instance group, a`broker-smoke-tests`
errand job, and both `broker-registrar` and `broker-deregistrar` errand jobs.

The Cassandra Service Broker implements the `cf create-service` verb as a
keyspace creation and the `cf bind-service` verb as a user creation.

The Cassandra Service Broker is purely stateless, so there is no need to setup
any database for it to work. This is possible thanks to a convention on naming
the created keyspaces and users. Keyspaces typically start with the `ks`
prefix. The Service Brokers only relies to the keyspaces and users in
Cassandra that follow its naming pattern.

The Service Broker comes with a `broker-smoke-tests` errand job that
implements a full round-trip around pushing an app in Cloud Foundry, binding a
Cassandra service to it, writing some data, and checking it can be read back.

The `broker-registrar` and `broker-deregistrar` errand jobs are provided by
the standard
[`broker-registrar` BOSH release](https://github.com/cloudfoundry-community/broker-registrar-boshrelease),
as provided by the Cloud Foundry community. Then you can register your broker
in CF with `bosh run-errand broker-registrar`.

If you are not familiar with the semantics of this `broker-registrar` BOSH
release, you just need to be warned that the `broker-deregistrar` job actually
purges all services offerings from Cloud Foundry, which can be pretty
destructive for your Clooud Foundry users. This is supposed to be used with
great caution.

With the `cf-service-broker.yml` operations file, we assumes that you are
using the *de facto* standard `cf-admin-user` job from the
[`collection-of-pullrequests` BOSH Release](https://github.com/cloudfoundry-community/collection-of-pullrequests-boshrelease),
(as provided by the Cloud Foundry community), wich is pretty useful at
providing a BOSH 2.0 Link that transmits Cloud Foundry settings to the Broker
(De-)Registrar errand jobs. If you don't use it, then you can provide values
for this Bosh Link manually. See the [Manual Linking](https://bosh.io/docs/links-manual.html)
section of the Bosh documentation for details about how to do this.


## `shield-v7-agent.yml`

This operations file installs a SHIELD v7 agent for backuping you Cassandra
cluster, one keyspace at a time. As a requisite, please note that the
`cassandra` SHIELD plugin was merged into v7 branch in the version `v7.0.4` of
the SHIELD Bosh Release.

With Cassandra, keyspaces usually have a replication factor that is strictly
less than the number of nodes in the cluster. This implies that a given
keyspace has to be backed up on *all* cluster nodes at the same time if you
want to capture all its data.

As SHIELD has no “cluster” abstraction, operators are required to create
separate one target and one job for each Cassandra node. To help in this
setup, the SHIELD auto-config that is provided here uses a mini-templated
syntax with idioms like `(ip)` and `(deployment)`.

The provisioned SHIELD jobs all use the same SHIELD schedule, so they are
started nearly at the same time. You don't get strict ACID consistency with
such a design but this is still a working backup solution.


## `shield-v8-agent.yml`

This operations file provides pretty much the same as its v7 counterpart, but
here for SHIELD v8. Please read the related documentation first. As a
requisite, please note that the `cassandra` SHIELD plugin was merged into v8
branch as of version `v8.0.5` of the SHIELD Bosh Release.

A few variables need to be specified, like the `((shield_domain))`which is an
IP address or a DNS name that Cassandra nodes can use to reach the SHIELD
“core” server. The `((shield-ca))` certificate should be the one from your
SHIELD deployment. We recommend you upload it in your CredHub config server
first, or use an absolute-path syntax that will target it in CredHub:

```
((/<bosh-director-name>/<shield-deployment-name>/shield-ca.certificate))
```


## `admin-tool.yml`

This operations file adds a job that provides convenient wrappers around usual
cassandra administrative tools. These are provided by this BOSH Release for
human convenience only, so they are definitely not required for a Cassandra
cluster to properly work. They are even not recommended in production, as they
can provide a larger attack surface to intruders.


## `bosh-lite.yml`

This operations file implements the necessary settings for deploying in Bosh-Lite.
