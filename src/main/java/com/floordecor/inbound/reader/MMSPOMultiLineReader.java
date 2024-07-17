package com.floordecor.inbound.reader;

import com.floordecor.inbound.consts.EntityConstants;
import com.floordecor.inbound.consts.POConstants;
import com.floordecor.inbound.service.CustomPropConfigService;
import com.floordecor.inbound.dto.enums.InterfaceTypes;
import com.floordecor.inbound.dto.mms.MMSPO;
import com.floordecor.inbound.dto.mms.MMSPOLine;
import com.floordecor.inbound.dto.mms.ORDCSTMFLD;
import com.floordecor.inbound.util.Utils;
import com.supplychain.foundation.batch.reader.ValidationAwareLineMapper;
import com.supplychain.foundation.exception.custom.FileValidationException;
import com.supplychain.foundation.logger.CustomLogger;
import com.supplychain.foundation.logger.CustomLoggerFactory;
import com.supplychain.foundation.utility.StringUtils;
import org.springframework.batch.item.*;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.support.SingleItemPeekableItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;


import java.util.*;

public class MMSPOMultiLineReader implements ItemStreamReader<MMSPO> {

    @Autowired private CustomPropConfigService configService;
    private final SingleItemPeekableItemReader<String> reader;
    @Value("#{jobParameters}")
    private Map<String, String> jobParameters;
    private final Map<String, DefaultLineMapper<?>> lineMappers = new HashMap<>();
    private int rowNumber = 1;
    private final Set<String> skippedLines = new LinkedHashSet<>();

    private static final CustomLogger log = CustomLoggerFactory.getLogger(MMSPOMultiLineReader.class);

    public MMSPOMultiLineReader(SingleItemPeekableItemReader<String> reader){
        this.reader = reader;
        lineMappers.put(POConstants.PO_HEADER,
                createLineMapper(
                        POConstants.PO_HEADER,
                        POConstants.PO_FIELD_INDEXES,
                        POConstants.PO_FIELD_NAMES,
                        MMSPO.class
                ));
        lineMappers.put(POConstants.PO_LINE_HEADER,
                createLineMapper(
                        POConstants.PO_LINE_HEADER,
                        POConstants.PO_LINE_INDEXES,
                        POConstants.PO_LINE_FIELDS,
                        MMSPOLine.class
                ));
        lineMappers.put(POConstants.PO_EXTENDED_HEADER,
                createLineMapper(
                        POConstants.PO_EXTENDED_HEADER,
                        POConstants.PO_EXTENDED_INDEXES,
                        POConstants.PO_EXTENDED_FIELDS,
                        ORDCSTMFLD.class
                ));
    }

    @Override
    public synchronized MMSPO read() throws Exception {
        String line=reader.read();
        if(line != null && line.startsWith("HDR"))
            line=reader.read();
        rowNumber++;
        int startRowNumber = rowNumber;
        skippedLines.clear();
        skippedLines.add(line);
        if (line == null) {
            return null;
        }
        try {
            MMSPO po = (MMSPO) lineMappers.get(POConstants.PO_HEADER).mapLine(line, rowNumber);
            Map<String,String> props= Utils.convertStringToMap(jobParameters.get(EntityConstants.PO_CONFIG_DB_PROP));
            String activeDestinationFacilityIds=props.get("ActiveDCs");
            if(!activeDestinationFacilityIds.contains(po.getDestinationFacilityId()))
                throw new Exception("Destination Facility Id is inactive!!");
            while (true) {
                line = reader.peek();
                String headerType;
                if (line != null
                        && !POConstants.PO_HEADER.equalsIgnoreCase((headerType = getHeaderType(line)))) {
                    rowNumber++;
                    skippedLines.add(line);
                    switch (headerType) {
                        case POConstants.PO_EXTENDED_HEADER -> po.getExtended()
                                .add(
                                        (ORDCSTMFLD)
                                                lineMappers.get(POConstants.PO_EXTENDED_HEADER).mapLine(line, rowNumber));
                        case POConstants.PO_LINE_HEADER ->{
                            MMSPOLine mappedLine = (MMSPOLine) lineMappers.get(POConstants.PO_LINE_HEADER).mapLine(line, rowNumber);
                            String uomId=mappedLine.getQuantityUomId();
                            mappedLine.setQuantityUomId(props.getOrDefault(uomId,uomId));
                            if(!mappedLine.getItemId().equalsIgnoreCase(mappedLine.getPurchaseOrderLineId())){
                                throw new InputMismatchException();
                            }
                            else{
                                po.getPurchaseOrderLine().add(mappedLine);
                            }
                        }
                    }
                    reader.read();
                } else {
                    return po;
                }
            }
        } catch (Exception ex) {
            int errorRowNumber = rowNumber;
            while ((line = reader.peek()) != null
                    && !POConstants.PO_HEADER.equalsIgnoreCase(getHeaderType(line))) {
                rowNumber++;
                skippedLines.add(reader.read());
            }
            log.error(
                    "Error processing {} lines {} to {} starting at line {}:",
                    InterfaceTypes.Purchase_Order,
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
    private <T> DefaultLineMapper<T> createLineMapper(
            String interfaceName,
            int[] includedIndexes,
            String[] includedFieldNames,
            Class<T> targetType) {
        return ValidationAwareLineMapper.<T>builder()
                .interfaceName(interfaceName)
                .delimiter(POConstants.DELIMITER)
                .includedIndexes(includedIndexes)
                .includedFieldNames(includedFieldNames)
                .targetType(targetType)
                .build()
                .lineMapper();
    }

    private static String getHeaderType(String line) {
        return StringUtils.remove(line.substring(0, line.indexOf(POConstants.DELIMITER)), "\"", "");
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

    public boolean validatePo(MMSPO po){

        return false;
    }
}
