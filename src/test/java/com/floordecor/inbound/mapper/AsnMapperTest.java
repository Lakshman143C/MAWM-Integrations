package com.floordecor.inbound.mapper;

import com.supplychain.mawm.dto.asn.Asn;
import com.supplychain.mawm.dto.asn.AsnLineDto;
import com.floordecor.inbound.dto.mms.ASNLine;
import com.floordecor.inbound.dto.mms.MMSAsn;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Execution(ExecutionMode.SAME_THREAD)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class AsnMapperTest {
    private final AsnMapper mapper = Mappers.getMapper(AsnMapper.class);

    @Test
    void toMAWMAsn() {

        MMSAsn source=new MMSAsn();
        source.setAsnId("MMS000396935");
        source.setAsnOriginTypeId("W");
        source.setCanceled(false);
        source.setDestinationFacilityId("991");
        source.setTransferNumber("TRANSFER");
        source.setContainerNumber("CONT23045");
        source.setEstimatedDeliveryDate(new Date("03/12/24 00:01"));

        Set<ASNLine> asnLines=new HashSet<>();
        ASNLine line=new ASNLine();
        line.setAsn(source);
        line.setAsnLineId("100115971");
        line.setCanceled(false);
        line.setShippedQty(720);
        line.setItemId("100115971");
        line.setPurchaseOrderId("100115971");
        asnLines.add(line);

        source.setAsnLines(asnLines);

        Asn result= mapper.toMAWMItem(source);
        assertNotNull(result);
        assertEquals("MMS000396935",result.getAsnId());
        assertEquals(false,result.isCanceled());
        assertEquals("W",result.getAsnOriginTypeId());
        assertEquals("991",result.getDestinationFacilityId());
        assertEquals("TRANSFER",result.getExtendedAttributes().getTransferNumber());
        assertEquals("CONT23045",result.getExtendedAttributes().getContainerNumber());
        for(AsnLineDto each:result.getAsnLines())
        {
            assertEquals("100115971",each.getAsnLineId());
            assertEquals("MMS000396935",each.getAsnId());
            assertEquals(false,each.isCanceled());
            assertEquals("100115971",each.getItemId());
            assertEquals("100115971",each.getPurchaseOrderId());
        }
    }
}
