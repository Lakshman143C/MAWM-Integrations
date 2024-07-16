package com.floordecor.inbound.consts;

public class AsnConstants {
    public static final String DELIMITER = "|";

    public static final String ASN_HEADER = "ASN";
    public static final int[] ASN_FIELD_INDEXES = {
            1, 3, 4, 5, 6, 7, 17, 19
    };
    public static final String[] ASN_FIELD_NAMES = {
            "asnId",
            "originFacilityId",
            "destinationFacilityId",
            "estimatedDeliveryDate",
            "transferNumber",
            "containerNumber",
            "asnOriginTypeId",
            "canceled"
    };

    public static final String ASNLINE_HEADER="ASNLINE";
    public static final int[] ASNLINE_FIELD_INDEXES={
            1, 2, 3, 4
    };
    public static final String[] ASNLINE_FIELD_NAMES={
            "asnLineId",
            "itemId",
            "shippedQty",
            "qtyUomId"
    };

}
