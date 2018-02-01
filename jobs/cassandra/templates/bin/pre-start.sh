#!/usr/bin/env bash

set +ex

<% if p('disable_linux_swap').to_s == "true" %>
echo "Deactivating Linux swap"
/sbin/swapoff --all
echo "Deactivated Linux swap"
<% end %>
