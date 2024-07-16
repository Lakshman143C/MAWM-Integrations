package com.floordecor.inbound.service;

import com.floordecor.inbound.TestUtils;
import com.floordecor.inbound.dto.enums.InterfaceTypes;
import com.floordecor.inbound.dto.enums.Source;
import com.floordecor.inbound.entity.TransactionStatus;
import com.floordecor.inbound.entity.transaction.InboundTransaction;
import com.floordecor.inbound.repository.InboundTransactionRepository;
import com.supplychain.foundation.consts.JobConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Execution(ExecutionMode.SAME_THREAD)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class InboundTransactionServiceTest {
    @InjectMocks private InboundTransactionService inboundTransactionService;
    @Mock private InboundTransactionRepository inboundTransactionRepository;

    @Test
    void t1_getByJobIdOrFileNameAndStatus() {
        InboundTransaction inboundTransaction =
                TestUtils.generateInboundTransaction(TransactionStatus.STARTED);
        when(inboundTransactionRepository.findByStatusAndIdOrFileName(
                TransactionStatus.STARTED, "uuid", "fileName.csv"))
                .thenReturn(Optional.of(inboundTransaction));
        Optional<InboundTransaction> transaction =
                inboundTransactionService.getByJobIdOrFileNameAndStatus(
                        "uuid", "fileName.csv", TransactionStatus.STARTED);
        assertTrue(transaction.isPresent());
        assertEquals(transaction.get(), inboundTransaction);
    }

    @Test
    void t1_save() {
        InboundTransaction inboundTransaction =
                TestUtils.generateInboundTransaction(TransactionStatus.STARTED);
        when(inboundTransactionRepository.save(inboundTransaction)).thenReturn(inboundTransaction);
        inboundTransactionService.save(inboundTransaction);
        verify(inboundTransactionRepository, times(1)).save(inboundTransaction);
    }

    @Test
    void t1_isFileAlreadyProcessed() {
        when(inboundTransactionRepository.existsByFileNameAndStatusNot(
                "fileName.csv", TransactionStatus.STARTED))
                .thenReturn(true);
        assertTrue(inboundTransactionService.isFileAlreadyProcessed("fileName.csv"));
        verify(inboundTransactionRepository, times(1))
                .existsByFileNameAndStatusNot("fileName.csv", TransactionStatus.STARTED);
    }

    @Test
    void t2_isFileAlreadyProcessed() {
        when(inboundTransactionRepository.existsByFileNameAndStatusNot(
                "fileName.csv", TransactionStatus.STARTED))
                .thenReturn(false);
        assertFalse(inboundTransactionService.isFileAlreadyProcessed("fileName.csv"));
        verify(inboundTransactionRepository, times(1))
                .existsByFileNameAndStatusNot("fileName.csv", TransactionStatus.STARTED);
    }

    @Test
    void t1_saveOrUpdateInboundTransaction() {
        Map<String, String> parameters = TestUtils.generateParameter();
        TransactionStatus transactionStatus = TransactionStatus.STARTED;
        when(inboundTransactionRepository.findByStatusAndIdOrFileName(
                transactionStatus, TestUtils.JOB_ID, TestUtils.FILE_NAME))
                .thenReturn(Optional.empty());
        inboundTransactionService.saveOrUpdateInboundTransaction(parameters, transactionStatus);
        ArgumentCaptor<InboundTransaction> inboundTransactionCaptor =
                ArgumentCaptor.forClass(InboundTransaction.class);
        verify(inboundTransactionRepository, times(1)).save(inboundTransactionCaptor.capture());
        InboundTransaction savedTransaction = inboundTransactionCaptor.getValue();
        assertEquals(TestUtils.JOB_ID, savedTransaction.getId());
        assertEquals(InterfaceTypes.Purchase_Order, savedTransaction.getInterfaceType());
        assertEquals(Source.MMS, savedTransaction.getSource());
        assertEquals(TestUtils.FILE_NAME, savedTransaction.getFileName());
        assertEquals(transactionStatus, savedTransaction.getStatus());
    }

    @Test
    public void t2_SaveOrUpdateInboundTransaction() {

        Map<String, String> parameters = TestUtils.generateParameter();
        TransactionStatus transactionStatus = TransactionStatus.STARTED;
        InboundTransaction existingTransaction =
                TestUtils.generateInboundTransaction(transactionStatus);
        when(inboundTransactionRepository.findByStatusAndIdOrFileName(
                transactionStatus, TestUtils.JOB_ID, TestUtils.FILE_NAME))
                .thenReturn(Optional.of(existingTransaction));
        inboundTransactionService.saveOrUpdateInboundTransaction(parameters, transactionStatus);
        verify(inboundTransactionRepository, times(1)).save(existingTransaction);
    }

    @Test
    public void t3_SaveOrUpdateInboundTransaction() {
        Map<String, String> parameters = TestUtils.generateParameter();
        parameters.put(JobConstants.TOTAL_PROCESSABLE_LINE_COUNT, "10");
        parameters.put(JobConstants.TOTAL_ROW_COUNT, "12");
        parameters.put(JobConstants.FAILURE_MESSAGE, "Failed to process file");
        InboundTransaction existingTransaction =
                TestUtils.generateInboundTransaction(TransactionStatus.STARTED);
        when(inboundTransactionRepository.findByStatusAndIdOrFileName(any(), any(), any()))
                .thenReturn(Optional.of(existingTransaction));
        when(inboundTransactionRepository.save(any())).thenReturn(existingTransaction);
        inboundTransactionService.saveOrUpdateInboundTransaction(
                parameters, TransactionStatus.COMPLETED);
        verify(inboundTransactionRepository, times(1)).findByStatusAndIdOrFileName(any(), any(), any());
        verify(inboundTransactionRepository, times(1)).save(any());
        assertThat(existingTransaction.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(existingTransaction.getProcessedCount()).isEqualTo(10);
        assertThat(existingTransaction.getTotalCount()).isEqualTo(12);
        assertThat(existingTransaction.getMessage()).isEqualTo("Failed to process file");
    }

}