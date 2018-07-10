Cassandra BOSH release
======================

This BOSH Release allows you to easily roll-out and maintain
[Cassandra](http://cassandra.apache.org/) clusters, with the power of
[BOSH](https://bosh.io).

In the `deployment/` directory, you'll find BOSH 2.0 deployment manifests and
operations files. They cover:

- Cloud Foundry integration, with [Service Broker](https://www.openservicebrokerapi.org)
  deployment, sanity tests & registration.

- Integration with the [SHIELD](https://shieldproject.io/) backup solution
  ([v7](https://github.com/gstackio/gk-shield-boshrelease/releases/tag/v7.0.4)
  and [v8](https://github.com/starkandwayne/shield-boshrelease/releases/tag/v8.0.9)).

- Support for client-server TLS encryption and server-side inter-nodes mutual TLS.

- BOSH-Lite support and other goodies for release authors.


## Prerequisites

You need to ensure your BOSH director has `post-deploy` scripts enabled. This
is usually the case with [standard bosh-deployment][std-bosh-deployment]. See
the [post-deploy documentation][post-deploy] for more information about this.

[post-deploy]: https://bosh.io/docs/post-deploy/#director-configuration
[std-bosh-deployment]: https://github.com/cloudfoundry/bosh-deployment/blob/92917e7/bosh.yml#L87


## Usage

Provided that you have a properly targeted BOSH director, here is how you
would deploy a Cassandra cluster:

```bash
git clone https://github.com/orange-cloudfoundry/cassandra-boshrelease.git
cd cassandra-boshrelease

bosh create-release
bosh upload-release

>> depl-state.yml; chmod 600 depl-state.yml # just making sure the secrets are not readable by everyone

bosh -d cassandra deploy deployment/cassandra.yml \
    --vars-file deployment/default-vars.yml \
    --vars-store depl-state.yml
```

In a Cloud Foundry environment, here is how you deploy the Cassandra cluster
with its Service Broker, and how you would register the latter to Cloud
Foundry:

```bash
bosh -d cassandra deploy deployment/cassandra.yml \
    --vars-file deployment/default-vars.yml \
    -o deployment/operations/cf-service-broker.yml \
    --vars-store depl-state.yml

bosh -d cassandra run-errand broker-registrar
```


## Deployment manifests

### BOSH 2.0 manifests

See the documentation in the  [`deployment`](./deployment) directory.


### BOSH 1.0 manifests

Example BOSH 1.0 manifests can be found in th `manifests/` subdirectory. These
are not ready-to-use manifests. They are meant to be examples only.


## Notes on backuping with SHIELD

The SHIELD v7 and v8 `cassandra` plugins are designed to help you backup your
Cassandra cluster, one keyspace at a time.

As a result of the backup strategy implemented by the SHIELD plugin, extra
space is required on the persistent disk. As a rule of the thumb, you should
provide twice the persistent storage required for your data.

You'll find further information on backuping Cassandra with SHIELD in the
[deployment manifests documentation](./deployment/README.md).


## Cassandra admin tools

These tools can be installed on Cassandra nodes by an optional BOSH Job, that
you can opt in with the `admin-tools.yml` operations file.

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
