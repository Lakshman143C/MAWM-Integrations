package com.floordecor.inbound.mapper;

import com.supplychain.mawm.dto.po.MAWMPO;
import com.supplychain.mawm.dto.po.MAWMPOLine;
import com.floordecor.inbound.dto.mms.MMSPO;
import com.floordecor.inbound.dto.mms.MMSPOLine;
import com.floordecor.inbound.dto.mms.ORDCSTMFLD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Execution(ExecutionMode.SAME_THREAD)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class POCustomMappingTest {

    private final POCustomMapping poCustomMapping=new POCustomMapping();

    @Test
    void toMAWMPO() {
        MMSPO source = MMSPO.builder()
                .purchaseOrderId("PO12345")
                .vendorId("VENDOR123")
                .originFacilityId("FAC123")
                .deliveryStartDate("11/06/23 00:01")
                .deliveryEndDate("03/21/24 23:59")
                .closed(false)
                .canceled(false)
                .destinationFacilityId("DEST123")
                .extended(Arrays.asList(
                        new ORDCSTMFLD("iShipsWith", "false"),
                        new ORDCSTMFLD("ShipsWith", ""),
                        new ORDCSTMFLD("BambooRose", "Y"),
                        new ORDCSTMFLD("BRStatus", "N")))
                .purchaseOrderLine(Arrays.asList(
                        MMSPOLine.builder()
                                .purchaseOrder("PO12345")
                                .purchaseOrderLineId("LINE1")
                                .itemId("ITEM1")
                                .orderQuantity(100)
                                .quantityUomId("EA")
                                .closed("false")
                                .canceled("false")
                                .putawayType("STANDARD")
                                .build()
                ))
                .build();

        MAWMPO result=poCustomMapping.toMAWMPO(source);

        // Assertions
        assertNotNull(result);
        assertEquals("PO12345", result.getPurchaseOrderId());
        assertEquals("VENDOR123", result.getVendorId());
        assertEquals("FAC123", result.getOriginFacilityId());
        assertEquals("2023-11-06", result.getDeliveryStartDate());
        assertEquals("2024-03-21", result.getDeliveryEndDate());
        assertEquals("DEST123", result.getDestinationFacilityId());

        // Assertions for extended fields
        assertNotNull(result.getExtended());
        assertFalse(false, result.getExtended().getIShipsWith());
        assertFalse(false, result.getExtended().getIShipsWith());
        assertEquals("Y", result.getExtended().getBambooRose());
        assertEquals("N", result.getExtended().getBrStatus());

        // Assertions for purchase order lines
        assertNotNull(result.getPurchaseOrderLine());
        assertEquals(1, result.getPurchaseOrderLine().size());
        MAWMPOLine line = result.getPurchaseOrderLine().get(0);
        assertEquals("PO12345", line.getPurchaseOrderId());
        assertEquals("LINE1", line.getPurchaseOrderLineId());
        assertEquals("ITEM1", line.getItemId());
        assertEquals(100, line.getOrderQuantity());
        assertEquals("EA", line.getQuantityUomId());
        assertFalse( line.isCanceled());
        assertFalse(line.isClosed());
        //assertEquals(false, line.getCanceled());
        assertEquals("STANDARD", line.getExtended().getPutawayType());
    }
}