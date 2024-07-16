package com.floordecor.inbound.writer;

import com.floordecor.inbound.TestUtils;
import com.floordecor.inbound.consts.PropertyConstants;
import com.supplychain.mawm.dto.asn.Asn;
import com.floordecor.inbound.dto.enums.InterfaceTypes;
import com.supplychain.mawm.prop.MAWMProp;
import com.supplychain.foundation.prop.JobProp;
import com.supplychain.foundation.service.MessagingService;
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
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Execution(ExecutionMode.SAME_THREAD)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class AsnWriterTest {
    @InjectMocks
    private AsnWriter asnWriter;
    @Mock
    private MessagingService messagingService;
    @Mock private JobProp jobProp;
    @Mock private MAWMProp mawmProp;

    @BeforeEach
    void setUp() {
        when(jobProp.get(PropertyConstants.PROP_MMS_ASN))
                .thenReturn(
                        JobProp.JobDetail.builder()
                                .destination(
                                        JobProp.Destination.builder()
                                                .queues(
                                                        new HashMap<>() {
                                                            {
                                                                put(
                                                                        PropertyConstants.PROP_MMS_ASN,
                                                                        new JobProp.QueueInfo());
                                                            }
                                                        })
                                                .build())
                                .build());
        doNothing().when(messagingService).send(anyString(), any(), any(), any());
    }

    @Test
    void t1_syncAsnWriter() throws Exception {
        ItemWriter<Asn> itemWriter = getAsnWriter();
        itemWriter.write(new Chunk<>(List.of((new Asn()))));
        verify(messagingService, times(1)).send(anyString(), any(), any(), any());
    }
    private ItemWriter<Asn> getAsnWriter() {
        return asnWriter.syncAsnWriter(
                TestUtils.JOB_ID,
                InterfaceTypes.MMS_ASN.name(),
                TestUtils.FILE_NAME,
                jobProp,
                mawmProp);
    }

}
