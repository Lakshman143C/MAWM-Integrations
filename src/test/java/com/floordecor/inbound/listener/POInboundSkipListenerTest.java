package com.floordecor.inbound.listener;

import com.floordecor.inbound.TestUtils;
import com.floordecor.inbound.dto.enums.InterfaceTypes;
import com.supplychain.foundation.consts.JobConstants;
import com.supplychain.foundation.exception.custom.FileValidationException;
import com.supplychain.foundation.utility.FileUtils;
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
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Execution(ExecutionMode.SAME_THREAD)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class POInboundSkipListenerTest {
    @InjectMocks private POInboundSkipListener poInboundSkipListener;
    @Mock private Map<String, String> jobParameters;

    @BeforeEach
    public void init() {

        ReflectionTestUtils.setField(
                poInboundSkipListener,
                "jobParameters",
                new HashMap<>() {
                    {
                        put(JobConstants.INPUT_FILE_NAME, "temp/po/work/test.csv");
                    }
                });

        ReflectionTestUtils.setField(poInboundSkipListener, "errorList", new ArrayList<>());
    }

    @Test
    void t2_afterStep_withSkip() {
        StepExecution stepExecution =
                new StepExecution(
                        InterfaceTypes.Purchase_Order.name(), TestUtils.createJobExecution(""));
        stepExecution.setReadSkipCount(0);
        stepExecution.setReadCount(1);
        stepExecution.setWriteCount(0);
        stepExecution.addFailureException(new Exception());
        ExitStatus exitStatus = poInboundSkipListener.afterStep(stepExecution);
        assertEquals(ExitStatus.FAILED, exitStatus);
    }

    @Test
    void t1_beforeStep() {
        poInboundSkipListener.beforeStep(
                new StepExecution(
                        InterfaceTypes.Purchase_Order.name(), TestUtils.createJobExecution("")));
    }

    @Test
    void t1_shouldSkip() {
        poInboundSkipListener.shouldSkip(new RuntimeException(""), 0);
    }

    @Test
    void t2_shouldSkip() {
        poInboundSkipListener.shouldSkip(
                new FileValidationException("message", null) {
                    {
                        startLineNumber(1);
                        endLineNumber(14);
                        erredLineNumber(1);
                        inputs(new ArrayList<>());
                    }
                },
                0);
    }

    @Test
    void t3_shouldSkip() {
        poInboundSkipListener.shouldSkip(new FlatFileParseException("message", "erroredLine"), 0);
        final File file = new File("temp/po/error/test.csv_failed");
        FileUtils.deleteFileIfExist(file);
    }
}