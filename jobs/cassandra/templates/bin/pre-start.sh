#!/usr/bin/env bash

set +ex

<% if p('disable_linux_swap').to_s == "true" %>
echo "Deactivating Linux swap"
/sbin/swapoff --all
echo "Deactivated Linux swap"
<% end %>


# cqlshrc is only accessed by the 'root' user from the 'post-start' script
chmod 600 /var/vcap/jobs/cassandra/root/.cassandra/cqlshrc


# This is the only way to have this pre-start script be executable
chmod +x /var/vcap/jobs/cassandra/bpm-prestart
