package com.floordecor.inbound.job.batch;

import com.floordecor.inbound.consts.PropertyConstants;
import com.supplychain.mawm.dto.po.MAWMPO;
import com.floordecor.inbound.dto.mms.MMSPO;
import com.floordecor.inbound.listener.POInboundJobListener;
import com.floordecor.inbound.listener.POInboundSkipListener;
import com.floordecor.inbound.reader.MMSPOMultiLineReader;
import com.supplychain.foundation.batch.listener.CommonListener;
import com.supplychain.foundation.batch.listener.FileShiftJobListener;
import com.supplychain.foundation.prop.JobProp;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@ConditionalOnProperty(
        value = "jobs." + PropertyConstants.PROP_MMS_PO + ".isEnabled",
        havingValue = "true")
public class MMSPOJobConfig {

    @Autowired private JobRepository jobRepository;

    @Autowired private PlatformTransactionManager platformTransactionManager;

    @Autowired private JobProp jobProp;

    @Autowired private MMSPOMultiLineReader mmspoMultiLineReader;
    @Autowired private ItemProcessor<MMSPO, MAWMPO> syncMMSPOProcessor;

    @Autowired private ItemWriter<MAWMPO> syncMMSPOWriter;
    @Autowired private POInboundJobListener poInboundJobListener;
    @Autowired private POInboundSkipListener poInboundSkipListener;

    @Autowired private CommonListener commonListener;

    @Autowired private FileShiftJobListener poFileShiftJobListener;

    @Bean
    public Step mmsPOStep(TaskExecutor jobPoolTaskExecutor) {
        return new StepBuilder("mmsPOStep", jobRepository)
                .allowStartIfComplete(true)
                .<MMSPO, MAWMPO>chunk(
                        jobProp.get(PropertyConstants.PROP_MMS_PO).getChunkSize(), platformTransactionManager)
                .reader(mmspoMultiLineReader)
                .processor(syncMMSPOProcessor)
                .writer(syncMMSPOWriter)
                .faultTolerant()
                .skipPolicy((SkipPolicy) poInboundSkipListener)
                .listener((StepExecutionListener) poInboundSkipListener)
                .listener((StepExecutionListener) commonListener)
                .taskExecutor(jobPoolTaskExecutor)
                .build();
    }

    @Bean
    public Job mmsPOJob(Step mmsPOStep) {
        return new JobBuilder("mmsPOJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(mmsPOStep)
                .listener((JobExecutionListener) commonListener)
                .listener((JobExecutionListener) poFileShiftJobListener)
                .listener((JobExecutionListener) poInboundJobListener)
                .build();
    }

}
