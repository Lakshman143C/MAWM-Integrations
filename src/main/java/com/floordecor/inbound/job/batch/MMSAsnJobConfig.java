package com.floordecor.inbound.job.batch;

import com.floordecor.inbound.consts.PropertyConstants;
import com.supplychain.mawm.dto.asn.Asn;
import com.floordecor.inbound.dto.mms.MMSAsn;
import com.floordecor.inbound.listener.AsnInboundJobListener;
import com.floordecor.inbound.listener.AsnInboundSkipListener;
import com.floordecor.inbound.reader.MMSAsnMultiLineReader;
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
        value = "jobs." + PropertyConstants.PROP_MMS_ASN + ".isEnabled",
        havingValue = "true")
public class MMSAsnJobConfig {
    @Autowired
    private JobRepository jobRepository;
    @Autowired private PlatformTransactionManager platformTransactionManager;
    @Autowired private JobProp jobProp;
    @Autowired private MMSAsnMultiLineReader mmsAsnMultiLineReader;
    @Autowired private ItemProcessor<MMSAsn, Asn> syncMMSAsnProcessor;
    @Autowired private FileShiftJobListener mmsAsnFileShiftJobListener;
    @Autowired private ItemWriter<Asn> syncMMSAsnWriter;
    @Autowired private AsnInboundJobListener asnInboundJobListener;
    @Autowired private AsnInboundSkipListener asnInboundSkipListener;
    @Autowired private CommonListener commonListener;

    @Bean
    public Job mmsAsnJob(Step mmsAsnStep) {
        return new JobBuilder("mmsAsnJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(mmsAsnStep)
                .listener((JobExecutionListener) commonListener)
                .listener((JobExecutionListener) mmsAsnFileShiftJobListener )
                .listener((JobExecutionListener) asnInboundJobListener)
                .build();
    }

    @Bean
    public Step mmsAsnStep(TaskExecutor jobPoolTaskExecutor) {
        return new StepBuilder("mmsAsnStep", jobRepository)
                .allowStartIfComplete(true)
                .<MMSAsn, Asn>chunk(
                        jobProp.get(PropertyConstants.PROP_MMS_ASN).getChunkSize(), platformTransactionManager)
                .reader(mmsAsnMultiLineReader)
                .processor(syncMMSAsnProcessor)
                .writer(syncMMSAsnWriter)
                .faultTolerant()
                .skipPolicy((SkipPolicy) asnInboundSkipListener)
                .listener((StepExecutionListener) asnInboundSkipListener)
                .listener((StepExecutionListener) commonListener)
                .taskExecutor(jobPoolTaskExecutor)
                .build();
    }
}
