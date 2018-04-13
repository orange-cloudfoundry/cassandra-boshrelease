package com.orange.oss.osb.cassandra.util;

import java.util.HashMap;
import java.util.Map;

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

    public static Map<String, Object> buildCredentials(String pContactPoints, String pPort, boolean ssl, String pKeyspaceName, String pRoleName, String pPasswordGenerated) {
        //"credentials":{
        //	"hostname":"192.168.30.150",
        //	"jdbcUrl":
        //	"jdbc:mysql://192.168.30.150:3306/cf_a84ef203_285b_415f_8feb_7c8b832e9b3e?user=7TQrKxF2fpfJtWIz\u0026password=HSHY8Ai3zCrlU58z",
        //	"name":"cf_a84ef203_285b_415f_8feb_7c8b832e9b3e",
        //	"password":"HSHY8Ai3zCrlU58z",
        //	"port":3306,
        //	"uri":
        //	"mysql://7TQrKxF2fpfJtWIz:HSHY8Ai3zCrlU58z@192.168.30.150:3306/cf_a84ef203_285b_415f_8feb_7c8b832e9b3e?reconnect=true",
        //	"username": "7TQrKxF2fpfJtWIz"
        //}
        Map<String, Object> credentials = new HashMap<String, Object>();
        String normalizedContactPoints = pContactPoints;
        if (pContactPoints.charAt(pContactPoints.length()-1) == ','){
            normalizedContactPoints = pContactPoints.substring(0, pContactPoints.length()-1);
        }
        credentials.put("contact-points", normalizedContactPoints);
        credentials.put("port", pPort);
        credentials.put("ssl", ssl);
        credentials.put("login", Converter.uuidToRoleName(pRoleName));
        credentials.put("password", pPasswordGenerated);
        credentials.put("keyspaceName", Converter.uuidToKeyspaceName(pKeyspaceName));
        String cassandraJdbcUrl = "jdbc:cassandra://" + normalizedContactPoints + ":" + pPort + "/" + Converter.uuidToKeyspaceName(pKeyspaceName);
        credentials.put("jdbcUrl", cassandraJdbcUrl);
        return credentials;
    }

}
