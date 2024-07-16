package com.floordecor.inbound.listener;

import com.floordecor.inbound.dto.enums.InterfaceTypes;
import com.floordecor.inbound.entity.TransactionStatus;
import com.floordecor.inbound.service.InboundTransactionService;
import com.supplychain.foundation.batch.utility.BatchUtils;
import com.supplychain.foundation.consts.FileConstants;
import com.supplychain.foundation.consts.JobConstants;
import com.supplychain.foundation.exception.custom.FileAlreadyProcessed;
import com.supplychain.foundation.logger.CustomLogger;
import com.supplychain.foundation.logger.CustomLoggerFactory;
import com.supplychain.foundation.service.SFTPService;
import com.supplychain.foundation.utility.FileUtils;
import com.supplychain.foundation.utility.JobUtils;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

@Component
public class BrAsnInboundJobListener implements JobExecutionListener {
    private static final CustomLogger log =
            CustomLoggerFactory.getLogger(AsnInboundJobListener.class);

    @Autowired
    private InboundTransactionService inboundTransactionService;
    @Autowired(required = false)
    @Qualifier("brAsnSftpService") private SFTPService brAsnSftpService;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.debug("---------------> Before job execution <---------------");
        Map<String, String> parameters = JobUtils.getParametersMap(jobExecution);
        InterfaceTypes interfaceType =
                InterfaceTypes.valueOf(parameters.get(JobConstants.INTERFACE_TYPE));
        log.info("Start processing job - {} - {}", interfaceType, parameters.get(JobConstants.INPUT_FILE_NAME));
        String fileName =
                Path.of(parameters.get(JobConstants.INPUT_FILE_NAME)).getFileName().toString();
        if (inboundTransactionService.isFileAlreadyProcessed(fileName)) {
            log.error("File been processed Already - {}; so silently ignored", fileName);
            throw new FileAlreadyProcessed(fileName);
        }
        inboundTransactionService.saveOrUpdateInboundTransaction(parameters, TransactionStatus.STARTED);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.debug("---------------> After job execution <---------------");
        Map<String, String> parameters = JobUtils.getParametersMap(jobExecution);
        InterfaceTypes interfaceType =
                InterfaceTypes.valueOf(parameters.get(JobConstants.INTERFACE_TYPE));
        String exitStatus = jobExecution.getExitStatus().getExitCode();

        int readCount = 0, writeCount = 0, skipCount = 0, readSkipCount = 0;
        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            readCount += stepExecution.getReadCount();
            writeCount += stepExecution.getWriteCount();
            skipCount += stepExecution.getSkipCount();
            readSkipCount += stepExecution.getReadSkipCount();
        }
        parameters.put(JobConstants.TOTAL_ROW_COUNT, String.valueOf(readCount + readSkipCount));
        parameters.put(JobConstants.TOTAL_PROCESSABLE_LINE_COUNT, String.valueOf(writeCount));
        String message;
        if (skipCount > 0) {
            message = BatchUtils.extractErrorMessage(jobExecution.getAllFailureExceptions());
            parameters.put(JobConstants.FAILURE_MESSAGE, message);
            Path inputPath = Path.of(parameters.get(JobConstants.INPUT_FILE_NAME));
            String fileName = inputPath.getFileName().toString();
            String localFailurePath = FileUtils.getFailureDirPath(inputPath, fileName);
            final File file = new File(localFailurePath);
            if (file.exists() && ExitStatus.COMPLETED.getExitCode().equalsIgnoreCase(exitStatus)) {
                SFTPService sftpService = getSftpService(interfaceType);
                sftpService.uploadFile(
                        file,
                        FileUtils.generatePath(
                                FileConstants.ROOT_PATH_FORMAT,
                                parameters.get(JobConstants.SOURCE_DIRECTORY),
                                FileConstants.ERROR),
                        FileUtils.getFailureFileName(fileName));
            }
            FileUtils.deleteFileIfExist(file);
        }

        if (ExitStatus.COMPLETED.getExitCode().equalsIgnoreCase(exitStatus)) {
            log.info(
                    "Processed job - {} - {}",
                    parameters.get(JobConstants.INTERFACE_TYPE),
                    parameters.get(JobConstants.INPUT_FILE_NAME));
            inboundTransactionService.saveOrUpdateInboundTransaction(
                    parameters, TransactionStatus.COMPLETED);
        } else if (ExitStatus.STOPPED.getExitCode().equalsIgnoreCase(exitStatus)
                || ExitStatus.FAILED.getExitCode().equalsIgnoreCase(exitStatus)) {
            message = BatchUtils.extractErrorMessage(jobExecution.getAllFailureExceptions());
            parameters.put(JobConstants.FAILURE_MESSAGE, message);
            log.error(
                    "Failed to process job - {} - {}", parameters.get(JobConstants.INTERFACE_TYPE), message);
            inboundTransactionService.saveOrUpdateInboundTransaction(
                    parameters, TransactionStatus.FAILED);
        }
    }

    private SFTPService getSftpService(InterfaceTypes interfaceType) {
        switch (interfaceType) {
            case BR_ASN-> {
                return brAsnSftpService;
            }
            default -> {
                return null;
            }
        }
    }
}
