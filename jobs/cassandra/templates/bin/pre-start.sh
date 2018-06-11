#!/usr/bin/env bash

set -ue -o pipefail


function prepend_datetime() {
    awk -W interactive '{ system("echo -n [$(date +%FT%T%z)]"); print " " $0 }'
}

exec \
    3>&1 \
    4>&2 \
    > >(prepend_datetime >&3) \
    2> >(prepend_datetime >&4)


<% if p('disable_linux_swap').to_s == "true" %>
echo "INFO: Deactivating Linux swap"
/sbin/swapoff --all
echo "INFO: Deactivated Linux swap"
<% end %>


# cqlshrc is only accessed by the 'root' user from the 'post-start' script
chmod 600 /var/vcap/jobs/cassandra/root/.cassandra/cqlshrc


# This is the only way to have this pre-start script be executable
chmod +x /var/vcap/jobs/cassandra/bpm-prestart
