package com.orange.oss.osb.cassandra.util;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;

public class ConverterTest {

    @Test
    public void testUuidToKeyspaceName() {
        //Given
        String input = "055d0899-018d-4841-ba66-2e4d4ce91f47";
        String expected = "ks055d0899_018d_4841_ba66_2e4d4ce91f47";

        //When
        String result = Converter.uuidToKeyspaceName(input);

        //Then
        assertEquals(expected, result);
    }

    @Test
    public void testKeyspaceNameToUuid() {
        //Given
        String input = "ks055d0899_018d_4841_ba66_2e4d4ce91f47";
        String expected = "055d0899-018d-4841-ba66-2e4d4ce91f47";

        //When
        String result = Converter.keyspaceNameToUuid(input);

        //Then
        assertEquals(expected, result);
    }

    @Test
    public void testUuidToRoleName() {
        //Given
        String input = "bbbbbbbb-ba66-4841-018d-2e4d4ce91f47";
        String expected = "rbbbbbbbb_ba66_4841_018d_2e4d4ce91f47";

        //When
        String result = Converter.uuidToRoleName(input);

        //Then
        assertEquals(expected, result);
    }

    @Test
    public void testRoleNameToUuid() {
        //Given
        String input = "rbbbbbbbb_ba66_4841_018d_2e4d4ce91f47";
        String expected = "bbbbbbbb-ba66-4841-018d-2e4d4ce91f47";

        //When
        String result = Converter.roleNameToUuid(input);

        //Then
        assertEquals(expected, result);
    }

    @Test
    public void testBuildCredentials() {
        //Given
        String contactPoints = "127.0.0.1,127.0.0.2,";
        String port = "9042";
        boolean ssl = false;
        String keyspaceName = "055d0899-018d-4841-ba66-2e4d4ce91f47";
        String roleName = "bbbbbbbb-ba66-4841-018d-2e4d4ce91f47";
        String passwordGenerated = "12345678";
        String jdbcUrl = "jdbc:cassandra://127.0.0.1,127.0.0.2:9042/ks055d0899_018d_4841_ba66_2e4d4ce91f47";

        //When
        Map<String,Object> credentials = Converter.buildCredentials(contactPoints, port, ssl, keyspaceName, roleName, passwordGenerated);

        //Then
        assertEquals("contact-points", contactPoints.substring(0, contactPoints.length()-1), credentials.get("contact-points"));
        assertEquals("port", port, credentials.get("port"));
        assertEquals("ssl", ssl, credentials.get("ssl"));
        assertEquals("login", Converter.uuidToRoleName(roleName), credentials.get("login"));
        assertEquals("password", passwordGenerated, credentials.get("password"));
        assertEquals("keyspaceName", Converter.uuidToKeyspaceName(keyspaceName), credentials.get("keyspaceName"));
        assertEquals("jdbcUrl", jdbcUrl, credentials.get("jdbcUrl"));
    }
}
