#!/bin/bash
# Cassandra operation tool
#set -x
CONF_FILE=./cassandra.conf
source "${CONF_FILE}"
NODE=''
OPERATION=''
PID=''
USER="$(whoami)"
export CASS_PWD="<%=properties.cassandra_injector.cass_pwd%>"

backup_data() {
    check_process
    if [[ "$?" != 0 ]]; then
        return 1
    fi

    OLD_IFS="${IFS}"
    IFS=','
    BACKUP_KEYSPACES=(${BACKUP_KEYSPACES})
    IFS="${OLD_IFS}"
    local keyspace_data_dir
    local snapshots_dir
    snapshots_dir='snapshots'
    local table
    local timestamp
    local backup_data_package
    for keyspace in ${BACKUP_KEYSPACES[@]}; do
        #cd "${CASSANDRA_BIN}"
        cd /var/vcap/jobs/cassandra_seed/bin
        ./node-tool.sh clearsnapshot "${keyspace}"
        if [[ "$?" != 0 ]]; then
            err "Can not clear snapshot of keyspace \033[33m${keyspace}\033[0m."
            continue
        fi
        ./node-tool.sh snapshot "${keyspace}"
        if [[ "$?" != 0 ]]; then
            err "Can not make snapshot of keyspace \033[33m${keyspace}\033[0m."
            continue
        fi

        keyspace_data_dir="${CASSANDRA_DATA}/${keyspace}"
        #keyspace_data_dir="/var/vcap/store/cassandra_seed/${CLUSTER_NAME}/data/${keyspace}"
        cd "${keyspace_data_dir}"
        for table_data_dir in `ls -F | grep /$`; do
            cd "${table_data_dir}"
            if [[ -d "${snapshots_dir}" ]]; then
                cd "${snapshots_dir}"
                table="${table_data_dir%-*}"

                for snapshot_dir in `ls -F | grep /$`; do
                    cd "${snapshot_dir}"
                    timestamp="${snapshot_dir%/*}"
                    backup_data_package="${NODE}-${keyspace}-${table}-${timestamp}.tar.gz"
                    out "Backup ${keyspace}.${table} from node ${NODE} to ${BACKUP_DATA_DIR}/${backup_data_package}."
                    tar zcf "${BACKUP_DATA_DIR}/${backup_data_package}" *
                    if [[ "$?" != 0 ]]; then
                        err "Can not pack \033[33m${BACKUP_DATA_DIR}/${backup_data_package}\033[0m."
                        ls | grep -v -E '.*\.db|.*\.json|*\.sha1|.*\.txt' | xargs rm -rf
                        break
                    fi
                done
            fi
            cd "${keyspace_data_dir}"
        done

        #cd "${CASSANDRA_BIN}"
        cd "/var/vcap/jobs/cassandra_seed/bin"
        ./node-tool.sh clearsnapshot "${keyspace}"
        if [[ "$?" != 0 ]]; then
            err "Can not clear snapshot of keyspace \033[33m${keyspace}\033[0m."
            continue
        fi
    done
    return 0
}

check_process() {
    local process_pid
    #process_pid="$(pgrep -u "${USER}" -f 'CassandraDaemon')"
    #process_pid="$(pgrep -u "vcap" -f 'CassandraDaemon')"
    process_pid="$( ps -ef|grep -v grep|grep 'CassandraDaemon'|awk '{print $2}')"
    local process_count
    if [[ -n "${process_pid}" ]]; then
        process_count="$(echo "${process_pid}" | wc -l)"
    else
        process_count=0
    fi

    if [[ "${process_count}" > 1 ]]; then
        err 'More than one Cassandra process is running.'
        PID=''
        return 1
    elif [[ "${process_count}" == 1 ]]; then
        out 'Cassandra is running.'
        PID="${process_pid}"
        return 0
    else
        out 'Cassandra is not running.'
        PID=-1
        return 1
    fi
}


check_status() {
    check_process
    if [[ "$?" != 0 ]]; then
        return 1
    fi

    #cd "${CASSANDRA_BIN}"
    cd "/var/vcap/jobs/cassandra_seed/bin"
    local result
    #result="$(./node-tool.sh status |grep -v grep|grep 'UN|JN|DN|?N'| awk '{print $1,$2}')"
    result="$(./node-tool.sh status |grep -v grep|grep -E 'UN|LN|JN|MN'| awk '{print $1,$2}')"
    if [[ -z "${result}" ]]; then
        err 'Can not get Cassandra status.'
        return 1
    fi
    echo ${result}

#    local i
#    i=1
#    local next_word
#    local datacenter
#    local node_status
#    local node_address
#    local abnormal
#    next_word="$(echo ${result} | cut -d ' ' -f "$i")"
#    while [ "${next_word}" != 'Note:' -a "${next_word}" != '' ]; do
#        if [[ "${next_word}" == 'Datacenter:' ]]; then
#            ((i++))
#            datacenter="$(echo ${result} | cut -d ' ' -f "$i")"
#            i="$(($i + 7))"
#            next_word="$(echo ${result} | cut -d ' ' -f "$i")"
#            continue
#        fi
#        node_status="$(echo ${result} | cut -d ' ' -f "$i")"
#        ((i++))
#        node_address="$(echo ${result} | cut -d ' ' -f "$i")"
#        if [[ "${node_status}" == 'Datacenter:' ]]; then
#            next_word="${node_status}"
#            continue
#        fi
#        if [[ "${node_status}" != "UN" ]]; then
#            if [[ -n "${abnormal}" ]]; then
#                abnormal="${abnormal}\n"
#            fi
#            abnormal="${abnormal}${datacenter} node ${node_address} (${node_status}) is not ok."
#        fi
#        ((i++))
#        next_word="$(echo ${result} | cut -d ' ' -f "$i")"
#    done
#    if [[ -n "${abnormal}" ]]; then
#        out "${abnormal}"
#        return 1
#    fi


    ./cql-sh.sh "${NODE}" -u cassandra -p $CASS_PWD -e "${DROP_KEYSPACE_CQL}" > /dev/null 2>&1 \
        && ./cql-sh.sh "${NODE}" -u cassandra -p $CASS_PWD -e "${CREATE_KEYSPACE_CQL}" > /dev/null 2>&1 \
        && ./cql-sh.sh "${NODE}" -u cassandra -p $CASS_PWD -e "${DROP_TABLE_CQL}" > /dev/null 2>&1 \
        && ./cql-sh.sh "${NODE}" -u cassandra -p $CASS_PWD  -e "${CREATE_TABLE_CQL}" > /dev/null 2>&1 \
        && ./cql-sh.sh "${NODE}" -u cassandra -p $CASS_PWD -e "${INSERT_CQL}" > /dev/null 2>&1 \
        && ./cql-sh.sh "${NODE}" -u cassandra -p $CASS_PWD -e "${SELECT_CQL}" > /dev/null 2>&1 \
        && ./cql-sh.sh "${NODE}" -u cassandra -p $CASS_PWD  -e "${DROP_KEYSPACE_CQL}" > /dev/null 2>&1 \
        && sudo rm -rf "${CASSANDRA_DATA}/data/${TEST_KEYSPACE}"
    if [[ "$?" != 0 ]]; then
        out "Cassandra is not ok."
        return 1
    else
        out "Cassandra is ok."
        return 0
    fi
}


err() {
    echo -e "[$(date +"%Y-%m-%d %H:%M:%S")]: $@" >&2
}

get_local_ip() {
    local local_ip
    local_ip="$(ifconfig -a | grep inet | grep -v 'inet6\|127.0.0.1' | awk '{print $2}' | tr -d 'addr:')"
    if [[ -z "${local_ip}" ]]; then
        err 'Can not get local ip.'
        exit 1
    fi
    echo "${local_ip}"
    return 0
}

out() {
    echo -e "[$(date +"%Y-%m-%d %H:%M:%S")]: $@"
}

print_usage() {
    echo "Usage: $0 {backup|remove|restore|status}"
}

remove_node() {
    check_process
    if [[ "$?" != 0 ]]; then
        return 1
    fi

    confirm_operation "${OPERATION}" "${NODE}"

    #cd "${CASSANDRA_BIN}"
    cd "/var/vcap/jobs/cassandra_seed/bin"
    ./node-tool.sh decommission
    if  [[ "$?" == 0 ]]; then
        out "Node ${NODE} is removed from ${CLUSTER_NAME}."
        return 0
    else
        err "Can not remove node \033[33${NODE}\033[0m from ${CLUSTER_NAME}."
        return 1
    fi

}


restore_data() {
    check_process
    if [[ "$?" != 0 ]]; then
        return 1
    fi

    local restore_source_node
    restore_source_node="$(echo "${RESTORE_DATA_PACKAGE##*/}" | awk -F '-' '{print $1}')"
    local keyspace
    keyspace="$(echo "${RESTORE_DATA_PACKAGE##*/}" | awk -F '-' '{print $2}')"
    local table
    table="$(echo "${RESTORE_DATA_PACKAGE##*/}" | awk -F '-' '{print $3}')"
    local timestamp
    timestamp="$(echo "${RESTORE_DATA_PACKAGE##*/}" | awk -F '-' '{print $4}')"
    local current_dir
    #current_dir="$(cd "$(dirname $0)"; pwd)"
    cd "${RESTORE_DATA_DIR}"
    if [[ -z "${restore_source_node}" || -z "${keyspace}" || -z "${table}" || -z "${timestamp}" ]]; then
        err "Wrong name format \033[33${RESTORE_DATA_PACKAGE}\033[0m (expect: [node]-[keyspace]-[table]-[time].tar.gz)."
        return 1
    fi
    if [[ ! -d "${keyspace}/${table}" ]]; then
        mkdir -p "${keyspace}/${table}"
        if [[ "$?" != 0 ]]; then
            #err "Can not create \033[33${current_dir}/${keyspace}/${table}\033[0m."
            err "Can not create \033[33${RESTORE_DATA_DIR}/${keyspace}/${table}\033[0m."
            return 1
        fi
    else
        rm -rf "${keyspace}/${table}/*"
        if [[ "$?" != 0 ]]; then
            #err "Can not clean \033[33${current_dir}/${keyspace}/${table}\033[0m."
            err "Can not clean \033[33${RESTORE_DATA_DIR}/${keyspace}/${table}\033[0m."
            return 1
        fi
    fi

    #tar zxf "${RESTORE_DATA_PACKAGE}" -C "${keyspace}/${table}"
    tar zxf "${BACKUP_DATA_DIR}/${RESTORE_DATA_PACKAGE}" -C "${RESTORE_DATA_DIR}/${keyspace}/${table}"
    if [[ "$?" != 0 ]]; then
        err "Can not unpack \033[33${RESTORE_DATA_PACKAGE}\033[0m."
        rm -rf "${keyspace}/${table}"
        return 1
    fi

    cd "${CASSANDRA_BIN}"
    out "Restore ${keyspace}.${table} from ${RESTORE_DATA_PACKAGE}."
    local return_code
    #./sstableloader -d "${NODE}" "${current_dir}/${keyspace}/${table}"
    ./sstable-loader.sh -d "${NODE}" "${RESTORE_DATA_DIR}/${keyspace}/${table}"
    return_code="$?"
    if [[ "${return_code}" != 0 ]]; then
        err "Can not load data from \033[33${RESTORE_DATA_PACKAGE}\033[0m to \033[33${NODE}\033[0m."
    fi
    #rm -rf "${current_dir}/${keyspace}"
    rm -rf "${RESTORE_DATA_DIR}/${keyspace}"
    if [[ "$?" != 0 ]]; then
        #err "Can not delete \033[33${current_dir}/${keyspace}\033[0m."
        err "Can not delete \033[33${RESTORE_DATA_DIR}/${keyspace}\033[0m."
    fi
    return "${return_code}"
}


main() {
    out 'Cassandra operation tool.'

    OPERATION="$1"
    if [[ -z "${OPERATION}" ]]; then
        print_usage
        return 1
    fi
    readonly OPERATION
    shift 1

    NODE="$(get_local_ip)"
    readonly NODE
    readonly USER

    case "${OPERATION}" in
        add)
            add_node
            if [[ "$?" != 0 ]]; then
                check_process > /dev/null 2>&1
                if [[ "$?" != 0 ]]; then
                    sudo rm -rf "${CASSANDRA_HOME}"
                fi
            fi
            ;;
        backup)
            backup_data
            ;;
        remove)
            remove_node
            ;;
        restore)
            restore_data
            ;;
        status)
            check_status
            ;;
        *)
            err "Unexpected operation \033[33m${OPERATION}\033[0m."
            print_usage
            return 1
            ;;
    esac
}

main "$@"




