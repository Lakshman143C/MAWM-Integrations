package com.floordecor.inbound.integration;

import com.floordecor.inbound.consts.EntityConstants;
import com.floordecor.inbound.consts.PropertyConstants;
import com.floordecor.inbound.dto.enums.InterfaceTypes;
import com.floordecor.inbound.dto.enums.Source;
import com.floordecor.inbound.dto.prop.CustomPropConfigDto;
import com.floordecor.inbound.service.CustomPropConfigService;
import com.supplychain.foundation.consts.JobConstants;
import com.supplychain.foundation.logger.CustomLogger;
import com.supplychain.foundation.logger.CustomLoggerFactory;
import com.supplychain.foundation.prop.JobProp;
import com.supplychain.foundation.utility.*;
import lombok.Setter;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.integration.launch.JobLaunchRequest;
import org.springframework.batch.integration.launch.JobLaunchingGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizer;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizingMessageSource;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Map;

@Component
@ConditionalOnProperty(
        value = "jobs." + PropertyConstants.PROP_BR_ASN + ".isEnabled",
        havingValue = "true")
public class BRAsnIntegrationConfig {
    private static final CustomLogger log =
            CustomLoggerFactory.getLogger(MMSAsnIntegrationConfig.class);
    @Autowired
    private Job brAsnJob;
    @Autowired
    private JobProp jobProp;
    @Autowired private SessionFactory<SftpClient.DirEntry> brAsnInboundSftp;
    @Autowired private TaskExecutor filePollingTaskExecutor;
    @Autowired private JobRepository jobRepository;
    @Autowired private TaskExecutor jobTaskExecutor;
    @Bean
    public IntegrationFlow brAsnIntegrationFlow(SftpInboundFileSynchronizingMessageSource brAsnSftpSource)
    {
        return IntegrationFlow.from(brAsnSftpSource, IntegrationUtils.generatePoller(jobProp.get(PropertyConstants.PROP_BR_ASN), filePollingTaskExecutor))
                .enrichHeaders(headers->IntegrationUtils.enrichHeaders(headers,jobProp.get(PropertyConstants.PROP_BR_ASN)))
                .handle(IntegrationUtils.sftpRemoteFileMoveAdapter(brAsnInboundSftp))
                .channel(brAsnFileIn())
                .transform(brAsnFileMessageToJobRequest())
                .handle(brAsnJobLaunchingGateway())
                .channel("nullChannel")
                .log(LoggingHandler.Level.INFO)
                .get();
    }
    @Bean
    public SftpInboundFileSynchronizer brAsnSftpInboundSync() {
        return IntegrationUtils.inboundFileSynchronizer(
                brAsnInboundSftp,
                jobProp.get(PropertyConstants.PROP_BR_ASN).getRootDirectory()
                        + jobProp.get(PropertyConstants.PROP_BR_ASN).getDropDirectory(),
                jobProp.get(PropertyConstants.PROP_BR_ASN).getFilePattern(),
                30L);
    }

    @Bean
    public SftpInboundFileSynchronizingMessageSource brAsnSftpSource(
            SftpInboundFileSynchronizer brAsnSftpInboundSync) {
        return IntegrationUtils.inboundFileSftpSource(
                brAsnSftpInboundSync,
                jobProp.get(PropertyConstants.PROP_BR_ASN).getRootDirectory(),
                jobProp.get(PropertyConstants.PROP_BR_ASN).getFilePattern(),
                30L);
    }
    public DirectChannel brAsnFileIn() {
        return new DirectChannel();
    }

    private static Map<String,String> props;
    public brAsnFileToJobRequest brAsnFileMessageToJobRequest() {
        CustomPropConfigDto dto= customPropConfigService.getConfigPropertiesByConfigId(EntityConstants.ASN_CONFIG_DB_PROP);
        props=dto.getProperties();
        brAsnFileToJobRequest transformer = new brAsnFileToJobRequest();
        transformer.setJob(brAsnJob);
        transformer.setJobProp(jobProp);
        return transformer;
    }

    public JobLaunchingGateway brAsnJobLaunchingGateway() {
        TaskExecutorJobLauncher taskExecutorJobLauncher = new TaskExecutorJobLauncher();
        taskExecutorJobLauncher.setJobRepository(jobRepository);
        taskExecutorJobLauncher.setTaskExecutor(jobTaskExecutor);
        return new JobLaunchingGateway(taskExecutorJobLauncher);
    }
    @Autowired
    private CustomPropConfigService customPropConfigService;

    @Component
    @Setter
    public static class brAsnFileToJobRequest {
        private Job job;
        private JobProp jobProp;

        /** Preparing required parameter for the batch job */
        @Transformer
        public JobLaunchRequest jobLaunchRequest(Message<File> fileMessage) {
            JobParametersBuilder jobParameters = new JobParametersBuilder();
            jobParameters.addString(
                    JobConstants.INPUT_FILE_NAME, fileMessage.getPayload().getAbsolutePath());
            jobParameters.addString(JobConstants.JOB_ID, StringUtils.getUniqueId());
            jobParameters.addString(JobConstants.INTERFACE_TYPE, InterfaceTypes.BR_ASN.name());
            jobParameters.addString(JobConstants.SOURCE, Source.BR.name());
            jobParameters.addString(EntityConstants.ASN_CONFIG_DB_PROP,props.toString());
            jobParameters.addString(
                    JobConstants.ROOT_DIRECTORY,
                    jobProp.get(PropertyConstants.PROP_BR_ASN).getRootDirectory()
                            + jobProp.get(PropertyConstants.PROP_BR_ASN).getDropDirectory());
            jobParameters.addString(
                    JobConstants.SOURCE_DIRECTORY,
                    jobProp.get(PropertyConstants.PROP_BR_ASN).getRootDirectory());
            jobParameters.addString(
                    JobConstants.TRANSACTION_DATE,
                    DateUtils.getCurrentUTCTimeStampInString(DateUtils.LONG_DATE_FORMAT));
            LoggerUtils.setJobLogAttributes(JobUtils.getParametersMap(jobParameters));
            log.info("Triggering BR ASN job, jobParam - {}", jobParameters.toJobParameters());
            return new JobLaunchRequest(job, jobParameters.toJobParameters());
        }
    }
}
