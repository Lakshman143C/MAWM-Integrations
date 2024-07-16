package com.floordecor.inbound;

import com.floordecor.inbound.dto.enums.InterfaceTypes;
import com.floordecor.inbound.dto.enums.Source;
import com.floordecor.inbound.entity.TransactionStatus;
import com.floordecor.inbound.entity.transaction.InboundTransaction;
import com.supplychain.foundation.consts.JobConstants;
import com.supplychain.foundation.prop.JobProp;
import com.supplychain.foundation.utility.DateUtils;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParametersBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TestUtils {

    public static final String DEST_QUEUE_NAME = "po_inbound";
    public static final String ROOT_DIRECTORY = "inbound";
    public static final String FILE_NAME = "test.csv";
    public static final String JOB_ID = UUID.randomUUID().toString();
    public static final String TRAN_DATE =
            DateUtils.getCurrentUTCTimeStampInString(DateUtils.LONG_DATE_FORMAT);


    public static JobExecution createJobExecution(String failureMessage) {
        JobParametersBuilder jobParameters = new JobParametersBuilder();
        jobParameters.addString(JobConstants.JOB_ID, UUID.randomUUID().toString());
        jobParameters.addString(JobConstants.INPUT_FILE_NAME, FILE_NAME);
        jobParameters.addString(JobConstants.TRANSACTION_DATE, TRAN_DATE);
        jobParameters.addString(JobConstants.INTERFACE_TYPE, InterfaceTypes.Purchase_Order.name());
        jobParameters.addString(JobConstants.SOURCE_DIRECTORY, ROOT_DIRECTORY);
        jobParameters.addString(JobConstants.SOURCE, Source.MMS.name());
        jobParameters.addString(
                JobConstants.FAILURE_MESSAGE, failureMessage == null ? "" : failureMessage);
        JobExecution jobExecution = new JobExecution(1L, jobParameters.toJobParameters());
        jobExecution.setJobInstance(new JobInstance(0L, InterfaceTypes.Purchase_Order.name()));
        return jobExecution;
    }

    public static InboundTransaction generateInboundTransaction(TransactionStatus transactionStatus) {
        return InboundTransaction.builder()
                .fileName(FILE_NAME)
                .interfaceType(InterfaceTypes.Purchase_Order)
                .source(Source.MMS)
                .status(transactionStatus)
                .id(JOB_ID)
                .message("")
                .build();
    }

    public static Map<String, String> generateParameter() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(JobConstants.INPUT_FILE_NAME, FILE_NAME);
        parameters.put(JobConstants.JOB_ID, JOB_ID);
        parameters.put(JobConstants.INTERFACE_TYPE, InterfaceTypes.Purchase_Order.toString());
        parameters.put(JobConstants.SOURCE, Source.MMS.name());
        return parameters;
    }


    public static JobProp.JobDetail getJobProp(String key) {
        return JobProp.JobDetail.builder()
                .chunkSize(500)
                .rootDirectory(ROOT_DIRECTORY)
                .dropDirectory("/")
                .destination(
                        JobProp.Destination.builder()
                                .destType(JobProp.DestType.PUB_SUB)
                                .queues(
                                        new HashMap<>() {
                                            {
                                                put(
                                                        key,
                                                        JobProp.QueueInfo.builder()
                                                                .name(DEST_QUEUE_NAME)
                                                                .headers(
                                                                        new HashMap<>() {
                                                                            {
                                                                                put("MSG_TYPE", "test");
                                                                            }
                                                                        })
                                                                .build());
                                            }
                                        })
                                .build())
                .filePattern("*.CSV")
                .isenabled(true)
                .jobName(key)
                .maxFilesPerPoll(3)
                .pollFrequencyInSecond(5)
                .retryLimit(3)
                .build();
    }
}
