package com.floordecor.inbound.integration;

import com.floordecor.inbound.consts.EntityConstants;
import com.floordecor.inbound.consts.PropertyConstants;
import com.floordecor.inbound.customConfig.service.CustomPropConfigService;
import com.floordecor.inbound.dto.enums.InterfaceTypes;
import com.floordecor.inbound.dto.enums.Source;
import com.supplychain.foundation.consts.JobConstants;
import com.supplychain.foundation.logger.CustomLogger;
import com.supplychain.foundation.logger.CustomLoggerFactory;
import com.supplychain.foundation.prop.JobProp;
import com.supplychain.foundation.utility.*;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.integration.launch.JobLaunchRequest;
import org.springframework.batch.integration.launch.JobLaunchingGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

@Configuration
@NoArgsConstructor
public class MMSPOIntegrationConfig {

    private static final CustomLogger log = CustomLoggerFactory.getLogger(MMSPOIntegrationConfig.class);
    @Autowired private JobProp jobProp;
    @Autowired private Job mmsPOJob;
    @Autowired private SessionFactory<SftpClient.DirEntry> poInboundSftp;
    @Autowired private TaskExecutor filePollingTaskExecutor;
    @Autowired private JobRepository jobRepository;
    @Autowired private TaskExecutor jobTaskExecutor;
    @Autowired private CustomPropConfigService configService;

    @Bean
    public IntegrationFlow mmsPOIntegrationFlow(SftpInboundFileSynchronizingMessageSource
                                                     poSftpSource){
        return IntegrationFlow.from(
                        poSftpSource,
                        IntegrationUtils.generatePoller(jobProp.get(PropertyConstants.PROP_MMS_PO),filePollingTaskExecutor))
                .enrichHeaders(headers -> IntegrationUtils.enrichHeaders(
                        headers,jobProp.get(PropertyConstants.PROP_MMS_PO)))
                .handle(IntegrationUtils.sftpRemoteFileMoveAdapter(poInboundSftp))
                .channel(poFileIn())
                .transform(poFileToJobRequest())
                .handle(poJobLaunchingGateway())
                .channel("nullChannel")
                .log(LoggingHandler.Level.INFO)
                .get();
    }

    @Bean(name = "poSftpInboundSync")
    public SftpInboundFileSynchronizer poSftpInboundSync() {
        return IntegrationUtils.inboundFileSynchronizer(
                poInboundSftp,
                jobProp.get(PropertyConstants.PROP_MMS_PO).getRootDirectory()
                        + jobProp.get(PropertyConstants.PROP_MMS_PO).getDropDirectory(),
                jobProp.get(PropertyConstants.PROP_MMS_PO).getFilePattern(),
                30L);
    }

    @Bean
    public SftpInboundFileSynchronizingMessageSource poSftpSource(
            SftpInboundFileSynchronizer poSftpInboundSync) {
        return IntegrationUtils.inboundFileSftpSource(
                poSftpInboundSync,
                jobProp.get(PropertyConstants.PROP_MMS_PO).getRootDirectory(),
                jobProp.get(PropertyConstants.PROP_MMS_PO).getFilePattern(),
                30L);
    }

    public DirectChannel poFileIn() {
        return new DirectChannel();
    }

    private static Map<String,String> props;
    public poFileToJobRequest poFileToJobRequest(){
        props=configService.getConfigPropertiesByConfigId(EntityConstants.PO_CONFIG_DB_PROP).getProperties();
        poFileToJobRequest transformer=new poFileToJobRequest();
        transformer.setJob(mmsPOJob);
        transformer.setJobProp(jobProp);
        return transformer;
    }

    public JobLaunchingGateway poJobLaunchingGateway(){
        TaskExecutorJobLauncher taskExecutorJobLauncher = new TaskExecutorJobLauncher();
        taskExecutorJobLauncher.setJobRepository(jobRepository); //To-Do
        taskExecutorJobLauncher.setTaskExecutor(jobTaskExecutor); //To-Do
        return new JobLaunchingGateway(taskExecutorJobLauncher);
    }

    @Component
    @Setter
    public static class poFileToJobRequest {
        private Job job;
        private JobProp jobProp;

        /** Preparing required parameter for the batch job **/

        @Transformer
        public JobLaunchRequest jobLaunchRequest(Message<File> fileMessage) {
            JobParametersBuilder jobParameters = new JobParametersBuilder();
            jobParameters.addString(
                    JobConstants.INPUT_FILE_NAME, fileMessage.getPayload().getAbsolutePath());
            jobParameters.addString(JobConstants.JOB_ID, StringUtils.getUniqueId());
            jobParameters.addString(JobConstants.INTERFACE_TYPE, InterfaceTypes.Purchase_Order.name());
            jobParameters.addString(JobConstants.SOURCE, Source.MMS.name());
            jobParameters.addString(
                    JobConstants.ROOT_DIRECTORY,
                    jobProp.get(PropertyConstants.PROP_MMS_PO).getRootDirectory()
                            + jobProp.get(PropertyConstants.PROP_MMS_PO).getDropDirectory());
            jobParameters.addString(
                    JobConstants.SOURCE_DIRECTORY,
                    jobProp.get(PropertyConstants.PROP_MMS_PO).getRootDirectory());
            jobParameters.addString(
                    JobConstants.TRANSACTION_DATE,
                    DateUtils.getCurrentUTCTimeStampInString(DateUtils.LONG_DATE_FORMAT));
            jobParameters.addString(
                    EntityConstants.PO_CONFIG_DB_PROP,props.toString());
            LoggerUtils.setJobLogAttributes(JobUtils.getParametersMap(jobParameters));
            log.info("Triggering MMS po stage job, jobParam - {}", jobParameters.toJobParameters());
            return new JobLaunchRequest(job, jobParameters.toJobParameters());
        }
    }

}

