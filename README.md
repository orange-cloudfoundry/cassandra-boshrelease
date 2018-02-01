Cassandra BOSH release
======================

This BOSH Release allows you to easily roll-out and maintain
[Cassandra](http://cassandra.apache.org/) clusters, with the power of
[BOSH](https://bosh.io).

In the `deployment/` directory, you'll find BOSH 2.0 deployment manifests and
operations files. They cover:

- Cloud Foundry integration, with [Service Broker](https://www.openservicebrokerapi.org)
  deployment, sanity tests & registration.

- Integration with the [SHIELD](https://shieldproject.io/) backup solution (v7
  and v8).

- Bosh-Lite support.


## Usage

Provided that you have a properly targeted Bosh director, here is how you
would deploy a Cassandra cluster:

```bash
git clone https://github.com/orange-cloudfoundry/cassandra-cf-service-boshrelease.git cassandra-boshrelease
cd cassandra-boshrelease

bosh create-release
bosh upload-release

>> depl-state.yml; chmod 600 depl-state.yml # just making sure the secrets are not readable by everyone

bosh -d cassandra deploy deployment/cassandra.yml \
    --vars-store depl-state.yml
```

In a Cloud Foundry environment, here is how you deploy the Cassandra cluster
with its Service Broker, and how you would register the latter to Cloud
Foundry:

```bash
bosh -d cassandra deploy deployment/cassandra.yml \
    -o deployment/operations/cf-service-broker.yml \
    --vars-store depl-state.yml

bosh -d cassandra run-errand broker-registrar
```


## BOSH 2.0 deployment manifests

See the documentation in the  [`deployment`](./deployment) directory.


## BOSH 1.0 manifests

Example BOSH 1.0 manifests can be found in th `manifests/` subdirectory. These
are not ready-to-use manifests. They are meant to be examples only.


## Notes on backuping with SHIELD

The SHIELD v7 and v8 `cassandra` plugins are designed to help you backup your
Cassandra cluster, one keyspace at a time.

Please ensure that the `/var/vcap/cassandra/job/bin` directory is added to the
SHIELD v8 `env.path` configuration property. Indeed, this SHIELD plugin relies
on some `nodetool` and `sstableloader` wrapper scripts that are provided in
that directory.

As a result of the backup strategy implemented by the SHIELD plugin, extra
space is required on the persistent disk. As a rule of the thumb, you should
provide twice the persistent storage required for your data.


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
