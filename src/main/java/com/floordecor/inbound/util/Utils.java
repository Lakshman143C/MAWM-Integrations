package com.floordecor.inbound.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.HashMap;
import java.util.Map;

public class Utils {
    public static void printJson(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print the JSON

        try {
            String jsonString = mapper.writeValueAsString(object);
            System.out.println(jsonString);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            e.printStackTrace();
        }
    }
    public static Map<String, String> convertStringToMap(String str) {
        Map<String, String> map = new HashMap<>();

        // Remove the curly braces
        str = str.substring(1, str.length() - 1);

        // Split the string by commas
        String[] entries = str.split(", ");

        // Iterate over the entries
        for (String entry : entries) {
            // Split each entry by the equals sign
            String[] keyValue = entry.split("=");
            // Put the key-value pair into the map
            map.put(keyValue[0], keyValue[1]);
        }
        return map;
    }
}
