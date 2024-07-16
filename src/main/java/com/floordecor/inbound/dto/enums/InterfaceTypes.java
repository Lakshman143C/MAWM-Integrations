package com.floordecor.inbound.dto.enums;

import com.floordecor.inbound.consts.PropertyConstants;

import java.util.HashMap;
import java.util.Map;

public enum InterfaceTypes {
    MMS_ASN(PropertyConstants.PROP_MMS_ASN),
    BR_ASN(PropertyConstants.PROP_BR_ASN),
    MMS_ASN_STAGE(PropertyConstants.PROP_MMS_ASN_STAGE),

    BR_ASN_STAGE(PropertyConstants.PROP_BR_ASN_STAGE);
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
