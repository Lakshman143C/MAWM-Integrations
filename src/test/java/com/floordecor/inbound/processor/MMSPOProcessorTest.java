package com.floordecor.inbound.processor;


import com.supplychain.mawm.dto.po.MAWMPO;
import com.floordecor.inbound.dto.mms.MMSPO;
import com.floordecor.inbound.dto.prop.PoProps;
import com.floordecor.inbound.mapper.POCustomMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.batch.item.ItemProcessor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Execution(ExecutionMode.SAME_THREAD)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class MMSPOProcessorTest {

    @InjectMocks MMSPOProcessor mmspoProcessor;
    @Mock private PoProps props;
    @Mock private POCustomMapping poCustomMapping;
    private MMSPO mmspo;
    private MAWMPO mawmpo;

    @BeforeEach
    void setUp() {
        // Initialize test objects
        mmspo = new MMSPO();
        mmspo.setPurchaseOrderId("12345");

        mawmpo = new MAWMPO();
        mawmpo.setPurchaseOrderId("12345");

        // Mock the mapping method
        when(poCustomMapping.toMAWMPO(any(MMSPO.class))).thenReturn(mawmpo);
    }


    @Test
    void testSyncMMSPOProcessor() throws Exception {
        // Get the processor bean
        ItemProcessor<MMSPO, MAWMPO> processor = mmspoProcessor.syncMMSPOProcessor(poCustomMapping);

        // Process the mmspo object
        MAWMPO result = processor.process(mmspo);

        // Validate the result
        assertEquals("12345", result.getPurchaseOrderId());
    }
}