Cassandra release test plan
Unit tests
Templates to tests

Included:

    cassandre.yaml
    post-start.sh
    
Rendering technologies:

   Test templates with bosh-template 2.0.0 Gem

Configurations

    Mandatory
        replication mono datacenter
        replication multi-datacenter (si possible)

    Optional
        SSL or not
        Rac Aware or not
        

Acceptance tests

Write BOSH errand job.
Common tests

These test will be run for both un seul noeud and avec Replication 

    Read/Write test

    Before each:
        connect as admin
        create a keyspace
        create user with admin or without role on the database ('SUPERUSER')

    Test case:
        connect as user
        create table
        write some data in the table
        read the data back
        update the data
        delete data
        verify data is absent

    After each:
        drop keyspace
        drop user

    Backup/Restore

    Test case:
        write some data
        cassandra backup
        drop keyspace
        restore keyspace
        read the data back

    Replication factor (RF)= 3
        Verify that the data is well replicated
          by inserting some data into cassandra and verified that the correspoding token are also replicated on other nodes (2 if RF=3) 

    Soft Failover
        Build a 3-nodes cassandra : 1 seed and 2 servers
        Turn the resurrector off
        Stop on off the server node gracefully (stopdaemon)
        Verify that you still have the same result for the same CQL request ('select count(*) from keyspace_name.table_name')

    Cluster re-join after graceful stop

        Same begining as above
        Start the node back 
        verify it joins the cluster in Up and Running mode
        verify the data is here

    Dirty Failover
        Build a 3-nodes cassandra : 2 seeds and 2 servers
        Turn the resurrector on
        Kill one seed node 
        Verify that the last seed node takes and the rest of the cassandra cluster is always available

    Cluster recovery

        Same begining as above
        Wait for the resurrector to recreate the killed node
        verify

    Scale-in (general case)
        Build 3-nodes cassandra : 1 seed and 2 servers
        Scale in to 1 server node (decommission and removenode)
        Verify that deleted nodes have properly left the cluster

    Scale-in (involing a seed node)
        Build 3-nodes cassandra : 2 seed and 2 servers
        Scale in to 1 seed node (decommission and remove node)
        Verify that deleted seed node have properly left the cluster nd the rest of the cassandra cluster is always available

    Scale-out
        Build 3-nodes cassandra : 1 seed and 2 servers
        Scale out to 3 servers nodes
        Verify that new nodes join the cluster properly

    SSL
        Verify SSL is properly implemented



    ALTER USER
    CREATE USER
    DROP USER
    LIST USERS

