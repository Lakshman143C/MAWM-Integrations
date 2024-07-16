package com.floordecor.inbound.reader;

import com.supplychain.foundation.consts.JobConstants;
import lombok.NoArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.batch.item.support.SingleItemPeekableItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class MMSPOReader {

    @Bean
    @StepScope
    public MMSPOMultiLineReader mmspoMultiLineReader(
            @Value("#{jobParameters['" + JobConstants.INPUT_FILE_NAME + "']}") String resource){
        FlatFileItemReader<String> delegateReader = new FlatFileItemReaderBuilder<String>()
                .resource(new FileSystemResource(resource))
                .name("mmsPOReader")
                .lineMapper(new PassThroughLineMapper())
                .linesToSkip(0)
                .strict(true)
                .build();

        SingleItemPeekableItemReader<String> reader = new SingleItemPeekableItemReader<>();
        reader.setDelegate(delegateReader);

        return new MMSPOMultiLineReader(reader);
    }
}
