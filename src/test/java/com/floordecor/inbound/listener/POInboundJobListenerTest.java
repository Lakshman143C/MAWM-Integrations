package com.floordecor.inbound.listener;

import com.floordecor.inbound.TestUtils;
import com.floordecor.inbound.consts.PropertyConstants;
import com.floordecor.inbound.entity.TransactionStatus;
import com.floordecor.inbound.entity.transaction.InboundTransaction;
import com.floordecor.inbound.service.InboundTransactionService;
import com.supplychain.foundation.exception.custom.FileAlreadyProcessed;
import com.supplychain.foundation.prop.JobProp;
import org.junit.jupiter.api.Assertions;
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
import org.springframework.batch.core.JobExecution;

import java.util.Optional;

import static com.floordecor.inbound.TestUtils.FILE_NAME;
import static com.floordecor.inbound.TestUtils.generateInboundTransaction;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Execution(ExecutionMode.SAME_THREAD)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class POInboundJobListenerTest {

    @InjectMocks private POInboundJobListener jobListener;
    @Mock private JobProp jobProp;
    @Mock private InboundTransactionService inboundTransactionService;

    @BeforeEach
    public void init() {
        when(jobProp.get(PropertyConstants.PROP_MMS_PO))
                .thenReturn(TestUtils.getJobProp(PropertyConstants.PROP_MMS_PO));
    }

    @Test
    public void t1_jobBeforeExecution() throws Exception {
        JobExecution jobExecution = TestUtils.createJobExecution(null);
        when(inboundTransactionService.isFileAlreadyProcessed(FILE_NAME)).thenReturn(false);
        doNothing().when(inboundTransactionService).saveOrUpdateInboundTransaction(any(), any());
        jobListener.beforeJob(jobExecution);
        verify(inboundTransactionService, times(1)).isFileAlreadyProcessed(eq(FILE_NAME));
        verify(inboundTransactionService, times(1)).saveOrUpdateInboundTransaction(any(), any());
    }

    @Test
    void t2_beforeJob() {
        JobExecution jobExecution = TestUtils.createJobExecution(null);
        InboundTransaction inboundTransaction = generateInboundTransaction(TransactionStatus.STARTED);
        when(inboundTransactionService.isFileAlreadyProcessed(FILE_NAME)).thenReturn(true);
        doNothing().when(inboundTransactionService).saveOrUpdateInboundTransaction(any(), any());
        FileAlreadyProcessed exception =
                Assertions.assertThrows(
                        FileAlreadyProcessed.class,
                        () -> {
                            jobListener.beforeJob(jobExecution);
                        });
        assertNotNull(exception);
        assertEquals("File Already processed", exception.getMessage());
        verify(inboundTransactionService, times(1)).isFileAlreadyProcessed(eq(FILE_NAME));
        verify(inboundTransactionService, times(0)).saveOrUpdateInboundTransaction(any(), any());
    }

    @Test
    public void t3_jobBeforeExecution() throws Exception {
        JobExecution jobExecution = TestUtils.createJobExecution(null);
        InboundTransaction inboundTransaction = generateInboundTransaction(TransactionStatus.STARTED);
        when(inboundTransactionService.isFileAlreadyProcessed(FILE_NAME)).thenReturn(false);
        doNothing().when(inboundTransactionService).saveOrUpdateInboundTransaction(any(), any());
        jobListener.beforeJob(jobExecution);
        verify(inboundTransactionService, times(1)).isFileAlreadyProcessed(eq(FILE_NAME));
        verify(inboundTransactionService, times(1)).saveOrUpdateInboundTransaction(any(), any());
    }

    @Test
    public void t5_jobAfterExecution() throws Exception {
        JobExecution jobExecution = TestUtils.createJobExecution(null);
        jobExecution.setExitStatus(ExitStatus.COMPLETED);
        when(inboundTransactionService.getByJobIdOrFileNameAndStatus(
                anyString(), eq(FILE_NAME), eq(TransactionStatus.STARTED)))
                .thenReturn(Optional.empty());
        jobListener.afterJob(jobExecution);
    }

    @Test
    public void t6_jobAfterExecution() throws Exception {
        JobExecution jobExecution = TestUtils.createJobExecution(null);
        jobExecution.setExitStatus(ExitStatus.FAILED);
        InboundTransaction inboundTransaction = generateInboundTransaction(TransactionStatus.STARTED);
        when(inboundTransactionService.save(any())).thenReturn(inboundTransaction);
        jobListener.afterJob(jobExecution);
        verify(inboundTransactionService, times(1)).saveOrUpdateInboundTransaction(any(), any());
    }
}