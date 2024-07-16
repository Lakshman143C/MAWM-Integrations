package com.floordecor.inbound.reader;

import com.floordecor.inbound.consts.AsnConstants;
import com.floordecor.inbound.consts.EntityConstants;
import com.floordecor.inbound.dto.enums.InterfaceTypes;
import com.floordecor.inbound.dto.mms.ASNLine;
import com.floordecor.inbound.dto.mms.MMSAsn;
import com.floordecor.inbound.utility.UtilService;
import com.supplychain.foundation.batch.reader.ValidationAwareLineMapper;
import com.supplychain.foundation.exception.custom.FileValidationException;
import com.supplychain.foundation.logger.CustomLogger;
import com.supplychain.foundation.logger.CustomLoggerFactory;
import com.supplychain.foundation.service.SFTPService;
import com.supplychain.foundation.utility.StringUtils;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.support.SingleItemPeekableItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;

public class MMSAsnMultiLineReader implements ItemStreamReader<MMSAsn> {
    private static final CustomLogger log =
            CustomLoggerFactory.getLogger(MMSAsnMultiLineReader.class);
    @Value("#{jobParameters}")
    private Map<String, String> jobParameters;
    @Autowired(required = false)
    @Qualifier("mmsAsnSftpService") private SFTPService mmsAsnSftpService;
    @Autowired
    private UtilService utilService;
    private final SingleItemPeekableItemReader<String> reader;
    private final Map<String, DefaultLineMapper<?>> lineMappers = new HashMap<>();
    private final Set<String> skippedLines = new LinkedHashSet<>();
    private int rowNumber = 1;

    public MMSAsnMultiLineReader(SingleItemPeekableItemReader<String> reader) {
        this.reader = reader;
        lineMappers.put(
                AsnConstants.ASN_HEADER,
                createLineMapper(
                        AsnConstants.ASN_HEADER,
                        AsnConstants.ASN_FIELD_INDEXES,
                        AsnConstants.ASN_FIELD_NAMES,
                        MMSAsn.class));
        lineMappers.put(AsnConstants.ASNLINE_HEADER,
                createLineMapper(
                        AsnConstants.ASNLINE_HEADER,
                        AsnConstants.ASNLINE_FIELD_INDEXES,
                        AsnConstants.ASNLINE_FIELD_NAMES,
                        ASNLine.class));
    }

    private <T> DefaultLineMapper<T> createLineMapper(
            String interfaceName,
            int[] includedIndexes,
            String[] includedFieldNames,
            Class<T> targetType) {
        return ValidationAwareLineMapper.<T>builder()
                .interfaceName(interfaceName)
                .delimiter(AsnConstants.DELIMITER)
                .includedIndexes(includedIndexes)
                .includedFieldNames(includedFieldNames)
                .targetType(targetType)
                .build()
                .lineMapper();
    }

    private static String getHeaderType(String line) {
        return StringUtils.remove(line.substring(0, line.indexOf(AsnConstants.DELIMITER)), "\"", "");
    }

    @Override
    public synchronized MMSAsn read() throws Exception {
        String line = reader.read();
        rowNumber++;
        int startRowNumber = rowNumber;
        skippedLines.clear();
        skippedLines.add(line);
        if (line == null) {
            return null;
        }
        try {
            MMSAsn asn = (MMSAsn) lineMappers.get(AsnConstants.ASN_HEADER).mapLine(line, rowNumber);
            Map<String, String> job_props=new HashMap<>();
            if(jobParameters!=null) {
                job_props = utilService.convertStringToMap(jobParameters.get(EntityConstants.CONFIG_PROP));
                String activeFacilityIds = job_props.get("ActiveDCs");
                if (!activeFacilityIds.contains(asn.getDestinationFacilityId()))
                    throw new Exception("Destination Facility Id is inactive!!");
            }
            while (true) {
                line = reader.peek();
                String headerType;
                if (line != null
                        && !AsnConstants.ASN_HEADER.equalsIgnoreCase((headerType = getHeaderType(line)))) {
                    rowNumber++;
                    skippedLines.add(line);
                    switch (headerType) {
                        case AsnConstants.ASNLINE_HEADER -> {
                            asn.getAsnLines().add((ASNLine)lineMappers.get(AsnConstants.ASNLINE_HEADER).mapLine(line, rowNumber));
                        }
                    }
                    reader.read();
                } else {
                    if(jobParameters!=null) {
                        for (ASNLine each : asn.getAsnLines())
                            each.setQtyUomId(job_props.getOrDefault(each.getQtyUomId(), each.getQtyUomId()));
                    }
                    return asn;
                }
            }
        } catch (Exception ex) {
            int errorRowNumber = rowNumber;
            while ((line = reader.peek()) != null
                    && !AsnConstants.ASN_HEADER.equalsIgnoreCase(getHeaderType(line))) {
                rowNumber++;
                skippedLines.add(reader.read());
            }
            log.error(
                    "Error processing {} lines {} to {} starting at line {}:",
                    InterfaceTypes.MMS_ASN_STAGE,
                    startRowNumber,
                    rowNumber,
                    errorRowNumber);
            rowNumber--;
            throw new FileValidationException(ex.getMessage(), ex)
                    .startLineNumber(startRowNumber)
                    .erredLineNumber(errorRowNumber)
                    .endLineNumber(rowNumber)
                    .inputs(new ArrayList<>(skippedLines));
        }
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        reader.open(executionContext);
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        reader.update(executionContext);
    }

    @Override
    public void close() throws ItemStreamException {
        reader.close();
    }

}
