package com.floordecor.inbound.processor;

import com.supplychain.mawm.dto.asn.Asn;
import com.floordecor.inbound.dto.mms.ASNLine;
import com.floordecor.inbound.dto.mms.MMSAsn;
import com.floordecor.inbound.mapper.AsnMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.batch.item.ItemProcessor;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Execution(ExecutionMode.SAME_THREAD)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class MMSAsnProcessorTest {
    @InjectMocks
    private MMSAsnProcessor mmsAsnProcessor;
    private final AsnMapper asnMapper = Mappers.getMapper(AsnMapper.class);

    @Test
    void t1_syncMMSItemProcessor() throws Exception {
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

        ItemProcessor<MMSAsn,Asn> processor =mmsAsnProcessor.syncMMSAsnProcessor(asnMapper);
        Asn result=processor.process(source);

        assertNotNull(result);
        assertEquals("MMS000396935",result.getAsnId());
        assertEquals(false,result.isCanceled());
        assertEquals("W",result.getAsnOriginTypeId());
        assertEquals("991",result.getDestinationFacilityId());
        assertEquals("TRANSFER",result.getExtendedAttributes().getTransferNumber());
        assertEquals("CONT23045",result.getExtendedAttributes().getContainerNumber());

    }
}
