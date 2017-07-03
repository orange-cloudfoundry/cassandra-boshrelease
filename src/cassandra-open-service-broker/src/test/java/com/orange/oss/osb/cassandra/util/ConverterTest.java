package com.orange.oss.osb.cassandra.util;

import static org.junit.Assert.*;
import org.junit.Test;

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


}
