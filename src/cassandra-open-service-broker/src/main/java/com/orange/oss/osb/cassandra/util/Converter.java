package com.orange.oss.osb.cassandra.util;

public final class Converter {

    private static String KEYSPACE_PREFIX = "ks";
    private static String ROLE_PREFIX = "r";

    public static String uuidToKeyspaceName(String pUuid){
        String keyspaceName = KEYSPACE_PREFIX + pUuid.replace(Character.valueOf('-'), Character.valueOf('_'));
        return keyspaceName;
    }

    public static String keyspaceNameToUuid(String pKeyspaceName){
        String uuid = pKeyspaceName.substring(2).replace(Character.valueOf('_'), Character.valueOf('-'));
        return uuid;
    }

    public static String uuidToRoleName(String pUuid){
        String roleName = ROLE_PREFIX + pUuid.replace(Character.valueOf('-'), Character.valueOf('_'));
        return roleName;
    }

    public static String roleNameToUuid(String pRoleName){
        String uuid = pRoleName.substring(1).replace(Character.valueOf('_'), Character.valueOf('-'));
        return uuid;
    }
}
