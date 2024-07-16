package com.floordecor.inbound.listener;

import com.supplychain.foundation.consts.JobConstants;
import com.supplychain.foundation.exception.custom.FileValidationException;
import com.supplychain.foundation.logger.CustomLogger;
import com.supplychain.foundation.logger.CustomLoggerFactory;
import com.supplychain.foundation.utility.FileUtils;
import com.supplychain.foundation.utility.JobUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Setter
@Getter
@AllArgsConstructor
@StepScope
public class AsnInboundSkipListener implements SkipPolicy, StepExecutionListener {
    private static final CustomLogger log =
            CustomLoggerFactory.getLogger(AsnInboundSkipListener.class);

    private List<Throwable> errorList = new ArrayList<>();

    @Value("#{jobParameters}")
    private Map<String, String> jobParameters;

    @Override
    public boolean shouldSkip(Throwable t, long i) throws SkipLimitExceededException {
        if ((t instanceof FileValidationException fileParseException)) {
            log.warn(
                    "{} file row skipped; errorLineNumber - {}, input - {}, errorMsg - {}",
                    jobParameters.get(JobConstants.INTERFACE_TYPE),
                    fileParseException.erredLineNumber(),
                    fileParseException.inputs(),
                    fileParseException.getMessage());
            writeFile(fileParseException.inputs(), jobParameters.get(JobConstants.INPUT_FILE_NAME));
        } else if ((t instanceof FlatFileParseException fileParseException)) {
            log.warn(
                    "InventoryUpdate row skipped; lineNumber - {}, input - {}, errorMsg - {}",
                    fileParseException.getLineNumber(),
                    fileParseException.getInput(),
                    fileParseException.getMessage());
            writeFile(
                    List.of(fileParseException.getInput()), jobParameters.get(JobConstants.INPUT_FILE_NAME));
        } else {
            log.warn(
                    "{} file row skipped; errorMsg - {}",
                    jobParameters.get(JobConstants.INTERFACE_TYPE),
                    t.getMessage(),
                    t);
        }
        errorList.add(t);
        return Boolean.TRUE;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {}

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        Map<String, String> parameters = JobUtils.getParametersMap(stepExecution.getJobExecution());
        log.info(
                "---------------> After Step execution - {}; parameter - {}  <---------------",
                stepExecution.getStepName(),
                parameters);
        errorList.forEach(throwable -> stepExecution.getJobExecution().addFailureException(throwable));
        errorList.clear();
        return ((stepExecution.getWriteCount() != stepExecution.getReadCount())
                || (stepExecution.getReadCount() == 0
                && !stepExecution.getJobExecution().getFailureExceptions().isEmpty()))
                ? ExitStatus.FAILED
                : stepExecution.getExitStatus();
    }

    private void writeFile(final List<String> lines, final String filename) {
        Path filePath = Path.of(filename);
        String finalFilePath = FileUtils.getFailureDirPath(filePath, filePath.getFileName().toString());
        FileUtils.appendIntoFile(finalFilePath, String.join("\n", lines));
    }
}
