package com.floordecor.inbound.consts;

public class POConstants {
    public static final String DELIMITER = "|";

    public static final String PO_HEADER = "ORD";

    public static final int[] PO_FIELD_INDEXES = {3, 4, 5, 8, 9, 10, 18, 19};

    public static final String[] PO_FIELD_NAMES = {
            "purchaseOrderId",
            "canceled",
            "closed",
            "deliveryStartDate",
            "deliveryEndDate",
            "DestinationFacilityId",
            "originFacilityId",
            "vendorId",
    };

    public static final String PO_LINE_HEADER = "ORDLINE";

    public static final int[] PO_LINE_INDEXES = {1, 2,  10, 12, 33};

    public static final String[] PO_LINE_FIELDS = {
            "itemId",
            "purchaseOrderLineId",
            "orderQuantity",
            "quantityUomId",
            "putawayType"
    };

    public static final String PO_EXTENDED_HEADER = "ORDCSTMFLD";

    public static final int[] PO_EXTENDED_INDEXES = {1, 2};

    public static final String[] PO_EXTENDED_FIELDS = {
            "fieldName",
            "fieldValue"
    };
}
