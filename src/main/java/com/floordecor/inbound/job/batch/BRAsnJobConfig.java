package com.floordecor.inbound.job.batch;

import com.floordecor.inbound.consts.PropertyConstants;
import com.supplychain.mawm.dto.asn.Asn;
import com.floordecor.inbound.dto.br.Document;
import com.floordecor.inbound.listener.BrAsnInboundJobListener;
import com.floordecor.inbound.listener.BrAsnInboundSkipListener;
import com.floordecor.inbound.reader.BRAsnReader;
import com.supplychain.foundation.batch.listener.CommonListener;
import com.supplychain.foundation.batch.listener.FileShiftJobListener;
import com.supplychain.foundation.prop.JobProp;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
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
        value = "jobs." + PropertyConstants.PROP_BR_ASN + ".isEnabled",
        havingValue = "true")
public class BRAsnJobConfig {
    @Autowired
    private JobRepository jobRepository;
    @Autowired private PlatformTransactionManager platformTransactionManager;
    @Autowired private JobProp jobProp;
    @Autowired private BRAsnReader brAsnXmlReader;
    @Autowired private ItemProcessor<Document, Asn> syncBRAsnProcessor;

    @Autowired private ItemWriter<Asn> syncAsnWriter;
    @Autowired private BrAsnInboundJobListener brAsnInboundJobListener;

    @Autowired private FileShiftJobListener brAsnFileShiftJobListener;
    @Autowired private BrAsnInboundSkipListener brAsnInboundSkipListener;
    @Autowired private CommonListener commonListener;

    @Bean
    public Step brAsnStep(TaskExecutor jobPoolTaskExecutor) {
        return new StepBuilder("brAsnStep", jobRepository)
                .allowStartIfComplete(true)
                .<Document, Asn>chunk(
                        jobProp.get(PropertyConstants.PROP_BR_ASN).getChunkSize(),
                        platformTransactionManager)
                .reader(brAsnXmlReader)
                .processor(syncBRAsnProcessor)
                .writer(syncAsnWriter)
                .faultTolerant()
                .skipPolicy((SkipPolicy) brAsnInboundSkipListener)
                .listener((StepExecutionListener) brAsnInboundSkipListener)
                .listener((StepExecutionListener) commonListener)
                .taskExecutor(jobPoolTaskExecutor)
                .build();
    }

    @Bean
    public Job brAsnJob(Step brAsnStep) {
        return new JobBuilder("brAsnJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(brAsnStep)
                .listener((JobExecutionListener) commonListener)
                .listener((JobExecutionListener) brAsnFileShiftJobListener)
                .listener((JobExecutionListener) brAsnInboundJobListener)
                .build();
    }
}
