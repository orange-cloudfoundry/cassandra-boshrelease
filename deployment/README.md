# Base deployment

The base deployment suggested here is made of the `cassandra.yml` base
manifest and the `default-vars.yml` default variables.

This is a classical deployment with 3 seeds and optionally more servers. It
provides two separate instance groups for this, named `cassandra-seeds` and
`cassandra-servers`.

Cassandra “seeds” are plain Cassandra nodes with a special role in the
cluster: They are meant to “explain” the cluster topology (how data is spread
accross nodes) to any new node that enters the cluster. From the perspective
of a BOSH Release maintainer, the only particularity they have is to be listed
in the `seed_provider:` section of the `cassandra.yaml` configuration file.

When scaling out the cluster, you should add more instances (default is 0) of
the `cassandra-servers` instance group.

The default of 3 seeds is reasonable because less becomes risky to handle
situations when performing rolling upgrades or in case of failure.

By default, the `system_auth` keyspace (holding users and keyspaces) is
replicated as many times as there are cassandra seeds. So when you deploy
seeds only, you get one replication of it on all nodes.


# Operations files

## `use-bpm.yml`

This is a recommended way to strengthen the security of you Cassandra nodes in
production, implementing namespaces and cgroups to sandbox the execution of
Cassandra daemons. Using BPM (BOSH Process Manager) is meant to become the
default in future versions of this BOSH release.


## `rename-deployment-and-network.yml`

This helps in the classical operation that consists in renaming the deployment
and the network that instances are attached to.


## `cf-service-broker.yml`

This operations file add a `cassandra-brokers` instance group, a
`broker-smoke-tests` errand job, and both `broker-registrar` and
`broker-deregistrar` errand jobs.

The Cassandra Service Broker implements the `cf create-service` verb as a
keyspace creation and the `cf bind-service` verb as a user creation.

The Cassandra Service Broker is purely stateless, so there is no need to setup
any database for it to work. This is possible thanks to a convention on naming
the created keyspaces and users. Keyspaces typically start with the `ks`
prefix. The Service Brokers only relies to the keyspaces and users in
Cassandra that follow its naming pattern.

### Broker smoke tests

The Service Broker comes with a `broker-smoke-tests` errand job that
implements a full round-trip around pushing an app in Cloud Foundry, binding a
Cassandra service to it, writing some data, and checking it can be read back.

### Broker registration and purge

The `broker-registrar` and `broker-deregistrar` errand jobs are provided by
the standard
[`broker-registrar` BOSH release](https://github.com/cloudfoundry-community/broker-registrar-boshrelease),
as provided by the Cloud Foundry community. Then you can register your broker

in CF with `bosh run-errand broker-registrar`. The deployment option retained
here is to have those two errand jobs be deployed each one on its own VM. This
is recommended as a security perspective because the confguration files that
are rendered for running those errands typically contain admin passwords, so
it's best deleting them after their errand has run. From a performance
perspective though, you might be interedted in collocating those jobs on a
single service broker instance (this implies you have only one), so that you
don't pay the cost of creating a new VM when running them.

If you are not familiar with the semantics of this `broker-registrar` BOSH
release, there's one important thing to know. Be aware that the
`broker-deregistrar` job actually purges all services offerings from Cloud
Foundry, which can be pretty destructive for your Clooud Foundry users. This
is supposed to be used with great caution.

### Requirements on your Cloud Foundry deployment

With the `cf-service-broker.yml` operations file, we assumes that you are
using the *de facto* standard `cf-admin-user` job from the
[`collection-of-pullrequests` BOSH Release](https://github.com/cloudfoundry-community/collection-of-pullrequests-boshrelease),
(as provided by the Cloud Foundry community), which is pretty useful at
providing a BOSH 2.0 Link that lets the Broker (De-)Registrar errand jobs know
the exact Cloud Foundry settings. If you don't use it, then you can provide
values for this Bosh Link manually. See the [Manual Linking](https://bosh.io/docs/links-manual/)
section of the Bosh documentation for details about how to do this.


## `cf-service-broker-rename-deployment-and-network.yml`

This add-on to `cf-service-broker.yml` implements a deployment and network
renaming. To be used when using `rename-deployment-and-network.yml` and
`cf-service-broker.yml`.


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


# Development operations files

## `admin-tool.yml`

This operations file adds a job that provides convenient wrappers around usual
cassandra administrative tools. These are provided by this BOSH Release for
human convenience only, so they are definitely not required for a Cassandra
cluster to properly work. They are even not recommended in production, as they
can provide a larger attack surface to intruders.


## `latest-versions.yml`

This helps release authors to deploy their latest development release that is
uploaded to the BOSH director.


## `bosh-lite.yml`

This operations file implements the necessary settings for deploying in Bosh-Lite.


## `custom-release-name.yml`

This helps release authors to deploy Cassandra releases with custom names, as
produced by the `bosh create-release --name` command. This is especially
useful when release authors are developing new features for the BOSH release
at the same time using a single shared BOSH director.


# Experimental operations files

## `experimental/use-tls.yml`

This ops file enable TLS for client-to-node connection and enforces mutual TLS
(mTLS) for internode connections.

Alone, this ops file in not enough alone for enabling these features. First,
it requires the `rename-deployment-and-network.yml` ops file. Then, a
certificate chain must be specified adding one of `tls-certs-green.yml` or
`tls-certs-blue.yml`.

In a running cluster, the impact of re-deploying with TLS enabled is that
Cassandra nodes implementing TLS won't be able to connect to Cassandra nodes
implementing non-encrypted connections. Thus, the operation introduces a
split-brain in the cluster, where the smaller part progressively become the
larger one while BOSH re-restarts cluster nodes with TLS enabled.


## `experimental/tls-certs-green.yml`

This uses the “green” certificate chain for client-to-node TLS encryption and
internode mTLS. By “certificate chain”, we mean a server certificate and a
Certificate Authority. Transitioning from green to blue (forth or back)
certificates is only required when rotating certificates.

When rotating certificates from “green” to “blue”, the process is to use
`tls-certs-blue.yml` instead of `tls-certs-green.yml` and re-deploy.

If “blue” certificates have never been created, they will be generated by the
`bosh` CLI (when using a local file as `--vars-store`), or by the CredHub
server (if enabled). As a safety measure, you should always first ensure that
the “blue” certificates don't exist in your local variables store, or in your
CredHub server. If you need to use a custom Certificate Authority, then its
public certificate and private key will have to be specified as
`cassandra_ca_blue` variable in the local variables store, or uploaded for
this deployment variable in CredHub.

The impact of the certificate rotation operation is that Cassandra nodes
implementing “blue” certificate won't be able to connect to Cassandra nodes
implementing the “green” certificate. Thus, the certificate rotation introduce
a split-brain in the cluster, where the smaller part progressively become the
larger one while BOSH re-restarts cluster nodes with the new certificates.


## `experimental/tls-certs-blue.yml`

Same as the “green” certificate chain above, but this is the “blue” one. Which
only means that it's a different set of certificates. Transitioning from green
to blue certificates (forth or back) is only required when rotating
certificates.


## `experimental/rotate-certs-transitional-step.yml`

This experimental ops file is meant to be used as an intermediate step for a
smooth transition from green to blue certificates, or the reverse from from
blue to green.

This idea here is to run an intermediate deployment trusting both “green” and
“blue” certificates, before actually switching server certificates. This
intermediate step would be required temporarily, just for the sake of rotating
certificates.

But as it turns out, Cassandra doesn't support specifying many trusted
Certificate Authorities for mutual TLS. Even when those CAs are properly put
in the Java Truststore, connection issues appeared in the cluster between
nodes using different server certificates.

This experimental ops file is still kept here in case it turns out being
useful in the future.
