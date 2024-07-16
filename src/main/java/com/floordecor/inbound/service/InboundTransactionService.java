package com.floordecor.inbound.service;
import com.supplychain.foundation.consts.JobConstants;
import com.supplychain.foundation.logger.CustomLogger;
import com.supplychain.foundation.logger.CustomLoggerFactory;
import lombok.AllArgsConstructor;
import com.floordecor.inbound.dto.enums.InterfaceTypes;
import com.floordecor.inbound.dto.enums.Source;
import com.floordecor.inbound.entity.TransactionStatus;
import com.floordecor.inbound.entity.transaction.InboundTransaction;
import com.floordecor.inbound.repository.InboundTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
@Service
@AllArgsConstructor
public class InboundTransactionService {

    private static final CustomLogger log =
            CustomLoggerFactory.getLogger(InboundTransactionService.class);
    @Autowired
    private InboundTransactionRepository transactionRepository;

    public Optional<InboundTransaction> getByJobIdOrFileNameAndStatus(
            String jobId, String filename, TransactionStatus status) {
        return transactionRepository.findByStatusAndIdOrFileName(status, jobId, filename);
    }

    public InboundTransaction save(InboundTransaction inboundTransaction) {
        return transactionRepository.save(inboundTransaction);
    }

    public Boolean isFileAlreadyProcessed(String fileName) {
        return transactionRepository.existsByFileNameAndStatusNot(fileName, TransactionStatus.STARTED);
    }

    public void saveOrUpdateInboundTransaction(
            Map<String, String> parameters, TransactionStatus transactionStatus) {
        InboundTransaction inboundTransaction = null;
        String fileName =
                Path.of(parameters.get(JobConstants.INPUT_FILE_NAME)).getFileName().toString();
        String jobId = parameters.get(JobConstants.JOB_ID);
        String failureMsg = parameters.get(JobConstants.FAILURE_MESSAGE);
        inboundTransaction =
                InboundTransaction.builder()
                        .id(parameters.get(JobConstants.JOB_ID))
                        .interfaceType(InterfaceTypes.valueOf(parameters.get(JobConstants.INTERFACE_TYPE)))
                        .source(Source.valueOf(parameters.get(JobConstants.SOURCE)))
                        .fileName(fileName)
                        .status(transactionStatus)
                        .message(StringUtils.hasLength(failureMsg) ? failureMsg : null)
                        .build();
        Optional<InboundTransaction> transaction =
                getByJobIdOrFileNameAndStatus(jobId, fileName, TransactionStatus.STARTED);
        if (TransactionStatus.STARTED.equals(transactionStatus)) {
            if (transaction.isPresent()) {
                inboundTransaction = transaction.get();
                inboundTransaction.setMessage("Invalid Transaction");
                inboundTransaction.setStatus(TransactionStatus.FAILED);
            }

        } else {
            if (transaction.isPresent()) {
                inboundTransaction = transaction.get();
                inboundTransaction.setStatus(transactionStatus);
                inboundTransaction.setProcessedCount(
                        Integer.parseInt(
                                parameters.getOrDefault(JobConstants.TOTAL_PROCESSABLE_LINE_COUNT, "0")));
                inboundTransaction.setTotalCount(
                        Integer.parseInt(parameters.getOrDefault(JobConstants.TOTAL_ROW_COUNT, "0")));
                inboundTransaction.setMessage(StringUtils.hasLength(failureMsg) ? failureMsg : null);
            } else {
                log.warn(
                        "inboundTransaction record is not found , creating failure transaction , jobId - {}",
                        jobId);
            }
        }

        if (inboundTransaction != null) {
            save(inboundTransaction);
        }
    }
}