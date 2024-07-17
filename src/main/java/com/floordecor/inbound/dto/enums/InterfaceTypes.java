package com.floordecor.inbound.dto.enums;

import com.floordecor.inbound.consts.PropertyConstants;

import java.util.HashMap;
import java.util.Map;

public enum InterfaceTypes {
    Purchase_Order(PropertyConstants.PROP_MMS_PO),
    MMS_ASN(PropertyConstants.PROP_MMS_ASN),
    BR_ASN(PropertyConstants.PROP_BR_ASN);
    // Reverse-lookup map for getting a day from an abbreviation

    private static final Map<String, InterfaceTypes> lookup = new HashMap<>();

    static {
        for (InterfaceTypes type : InterfaceTypes.values()) {
            lookup.put(type.getType(), type);
        }
    }
    private final String type;
    InterfaceTypes(String type) {
        this.type = type;
    }

    public static InterfaceTypes get(String type) {
        return lookup.get(type);
    }
    public String getType() {
        return type;
    }
}
