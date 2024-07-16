package com.floordecor.inbound.utility;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class UtilService {
    public Map<String, String> convertStringToMap(String str) {
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
