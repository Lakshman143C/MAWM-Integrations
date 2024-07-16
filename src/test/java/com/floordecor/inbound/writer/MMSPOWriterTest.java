package com.floordecor.inbound.writer;


import com.supplychain.mawm.prop.MAWMProp;
import com.supplychain.mawm.dto.po.MAWMPO;
import com.supplychain.foundation.prop.JobProp;
import com.supplychain.foundation.service.MessagingService;
import org.assertj.core.util.diff.Chunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Execution(ExecutionMode.SAME_THREAD)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class MMSPOWriterTest {

    @Mock private MessagingService messagingService;
    @Mock private JobProp jobProp;
    @Mock private MAWMProp mawmProp;
    @InjectMocks private MMSPOWriter mmspoWriter;
    private String jobId = "jobId";
    private String interfaceType = "INTERFACE_TYPE";
    private String fileName = "fileName";
    private Chunk<MAWMPO> chunk;

    @BeforeEach
    void setUp() {
        List<MAWMPO> items = new ArrayList<>();
        items.add(new MAWMPO()); // Add your test objects
        Chunk<MAWMPO> chunk = new Chunk<>(items.size(),items);
    }
}