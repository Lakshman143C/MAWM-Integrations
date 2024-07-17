package com.floordecor.inbound.processor;

import com.floordecor.inbound.consts.DtoConstants;
import com.supplychain.mawm.dto.asn.Asn;
import com.floordecor.inbound.dto.br.Document;
import com.floordecor.inbound.dto.br.PackingListD;
import com.floordecor.inbound.dto.br.PackingListH;
import com.floordecor.inbound.dto.mms.ASNLine;
import com.floordecor.inbound.dto.mms.MMSAsn;
import com.floordecor.inbound.mapper.AsnMapper;
import com.supplychain.foundation.logger.CustomLogger;
import com.supplychain.foundation.logger.CustomLoggerFactory;
import com.supplychain.foundation.service.SFTPService;
import com.supplychain.foundation.utility.JsonUtils;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Configuration
public class BRAsnProcessor {
    @Autowired
    private AsnMapper asnMapper;
    private static final CustomLogger log = CustomLoggerFactory.getLogger(BRAsnProcessor.class);
    private final SimpleDateFormat dateFormatWithTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private final SimpleDateFormat dateFormatWithoutTime = new SimpleDateFormat("yyyy-MM-dd");
    @Autowired(required = false)
    @Qualifier("brAsnSftpService") private SFTPService brAsnSftpService;
    public Date unmarshal(String v) throws Exception {
        try {
            return dateFormatWithTime.parse(v);
        } catch (ParseException e) {
            return dateFormatWithoutTime.parse(v);
        }
    }

    public MMSAsn ConversionProcess(Document document) throws Exception {
        PackingListH packingListH = document.getPackingList().get(0).getPackingListHeaders().get(0);
        List<PackingListD> packingListDList = packingListH.getPackingListDetails();

        //Assigning Values from XML to Asn Entity
        MMSAsn asn = new MMSAsn();
        asn.setAsnId(packingListH.getDeliverTo()+"B"+packingListH.getPackingListNo().substring(4));
        asn.setAsnOriginTypeId(DtoConstants.ORIGIN_TYPE_ID);
        if(packingListH.getStatus().equals("APPROVED"))
            asn.setCanceled(false);
        else
            asn.setCanceled(true);
        asn.setDestinationFacilityId(packingListH.getDeliverTo());
        asn.setEstimatedDeliveryDate(unmarshal(packingListH.getDate5()));
        asn.setVendorId(packingListH.getSupplier());
        Set<ASNLine> getAsnLines=new HashSet<>();
        for(PackingListD packingListD:packingListDList)
        {
            double qty=Double.parseDouble(packingListD.getQty());
            ASNLine asnLines = new ASNLine();
            asnLines.setAsn(asn);
            asnLines.setAsnLineId(packingListD.getRowNo());
            asnLines.setBatchNumber(packingListD.getRowNo().length()>20?packingListD.getRowNo().substring(0,20):packingListD.getRowNo());
            asnLines.setInventoryAttribute1(packingListD.getMemo4().length()>10?packingListD.getMemo4().substring(0,10):packingListD.getMemo4());
            asnLines.setInventoryAttribute2(packingListD.getDate1().length()>10?packingListD.getDate1().substring(0,10):packingListD.getDate1());
            asnLines.setItemId(packingListD.getItemNo());
            asnLines.setPurchaseOrderId(packingListD.getOrderNo());
            asnLines.setPurchaseOrderLineId(packingListD.getItemNo());
            asnLines.setQtyUomId(DtoConstants.QTY_UOM_ID);
            asnLines.setShippedQty((int)Math.round(qty));
            if(qty>0)
                getAsnLines.add(asnLines);
        }
        asn.setAsnLines(getAsnLines);
        return asn;
    }

    @Bean
    public ItemProcessor<Document, Asn> syncBRAsnProcessor(AsnMapper asnMapper) {
        return document -> {
            Asn mawmAsn= asnMapper.toMAWMAsn(ConversionProcess(document));
            log.debug("Converted MAWM Asn , {}", JsonUtils.convert(mawmAsn));
            return mawmAsn;
        };
    }

}
