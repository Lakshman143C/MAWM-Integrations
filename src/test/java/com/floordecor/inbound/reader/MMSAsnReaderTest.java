package com.floordecor.inbound.reader;

import com.floordecor.inbound.dto.mms.ASNLine;
import com.floordecor.inbound.dto.mms.MMSAsn;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.batch.item.ExecutionContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Execution(ExecutionMode.SAME_THREAD)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class MMSAsnReaderTest {
    @InjectMocks
    private AsnReader asnReader;

    @Test
    void t1_mmsAsnMultiLineReader() throws Exception{
        MMSAsnMultiLineReader mmsAsnMultiLineReader=asnReader.mmsAsnMultiLineReader("src/test/resources/samples/mms-asn/FilePolling.140724.ASN-140724.csv");
        mmsAsnMultiLineReader.open(new ExecutionContext());
        MMSAsn mmsAsn = mmsAsnMultiLineReader.read();
        assert mmsAsn != null;
        assertEquals("MMS000396934", mmsAsn.getAsnId());
        List<String> asnLineIds=new ArrayList<>();
        for(ASNLine asnLine:mmsAsn.getAsnLines())
            asnLineIds.add(asnLine.getAsnLineId());
        System.out.println(asnLineIds);
        assertTrue(asnLineIds.contains("100115934"));
        mmsAsnMultiLineReader.close();
    }
}
