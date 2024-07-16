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

import java.io.IOException;

@Component
@NoArgsConstructor
public class AsnReader {

    @Bean
    @StepScope
    public MMSAsnMultiLineReader mmsAsnMultiLineReader(
            @Value("#{jobParameters['" + JobConstants.INPUT_FILE_NAME + "']}") String resource) {
        FlatFileItemReader<String> delegateReader =
                new FlatFileItemReaderBuilder<String>()
                        .resource(new FileSystemResource(resource))
                        .name("asnReader")
                        .lineMapper(new PassThroughLineMapper())
                        .linesToSkip(1)
                        .strict(true)
                        .build();

        SingleItemPeekableItemReader<String> reader = new SingleItemPeekableItemReader<>();
        reader.setDelegate(delegateReader);

        return new MMSAsnMultiLineReader(reader);
    }
    @Bean
    @StepScope
    public BRAsnReader brAsnXmlReader(
            @Value("#{jobParameters['" + JobConstants.INPUT_FILE_NAME + "']}") String resource) throws IOException {
        return new BRAsnReader(resource);
    }

}
