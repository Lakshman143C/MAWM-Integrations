package com.floordecor.inbound.reader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.batch.item.support.SingleItemPeekableItemReader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Execution(ExecutionMode.SAME_THREAD)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class MMSPOReaderTest {

    @InjectMocks private MMSPOReader mmsPOReader;
    @Mock private FileSystemResource resource;
    @Mock private SingleItemPeekableItemReader<String> singleItemPeekableItemReader;
    @Mock private FlatFileItemReader<String> flatFileItemReader;

    @BeforeEach
    public void setUp() {
        flatFileItemReader = new FlatFileItemReaderBuilder<String>()
                .resource(resource)
                .name("mmsPOReader")
                .lineMapper(new PassThroughLineMapper())
                .linesToSkip(0)
                .strict(true)
                .build();

        singleItemPeekableItemReader = new SingleItemPeekableItemReader<>();
        singleItemPeekableItemReader.setDelegate(flatFileItemReader);
    }

    @Test
    public void testMmspoMultiLineReader() {
        MMSPOMultiLineReader reader = mmsPOReader.mmspoMultiLineReader("src/test/resources/PO/");

        assertNotNull(reader);
        SingleItemPeekableItemReader<String> delegateReader = (SingleItemPeekableItemReader<String>) ReflectionTestUtils.getField(reader, "reader");
        assertNotNull(delegateReader);

        FlatFileItemReader<String> actualDelegate = (FlatFileItemReader<String>) ReflectionTestUtils.getField(delegateReader, "delegate");
        assertNotNull(actualDelegate);
    }
}