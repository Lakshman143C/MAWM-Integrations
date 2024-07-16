package com.floordecor.inbound.mapper;

import com.supplychain.mawm.dto.po.*;
import com.floordecor.inbound.dto.mms.MMSPO;
import com.floordecor.inbound.dto.mms.MMSPOLine;
import com.floordecor.inbound.dto.mms.ORDCSTMFLD;
import com.floordecor.inbound.util.DateTimeUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
@Primary
public class POCustomMapping {
    public MAWMPO toMAWMPO(MMSPO mmspo){

        MAWMPO mawmpo=new MAWMPO();
        List<MMSPOLine> mmspoLines=mmspo.getPurchaseOrderLine();
        List<ORDCSTMFLD> extendedFields=mmspo.getExtended();
        List<MAWMPOLine> mawmpoLines=new ArrayList<>();
        POExtended poExtended=new POExtended();

        //header level mappings
        mawmpo.setPurchaseOrderId(mmspo.getPurchaseOrderId());
        mawmpo.setOriginFacilityId(mmspo.getOriginFacilityId());
        mawmpo.setVendorId(mmspo.getVendorId());
        mawmpo.setDestinationFacilityId(mmspo.getDestinationFacilityId());
        mawmpo.setDeliveryStartDate(DateTimeUtils.convertToDate(mmspo.getDeliveryStartDate()));
        mawmpo.setDeliveryEndDate(DateTimeUtils.convertToDate(mmspo.getDeliveryEndDate()));
        mawmpo.setClosed(mmspo.getClosed());
        mawmpo.setCanceled(mmspo.getCanceled());
        //setting extendedField values
        for (ORDCSTMFLD field:extendedFields){
            switch (field.getFieldName()) {
                case "iShipsWith" -> poExtended.setIShipsWith(field.getFieldValue());
                case "ShipsWith" -> poExtended.setShipsWith(field.getFieldValue());
                case "BambooRose" -> poExtended.setBambooRose(field.getFieldValue());
                case "BRStatus" -> poExtended.setBrStatus(field.getFieldValue());
                default -> { }
            }
        }
        mawmpo.setExtended(poExtended);
        for (MMSPOLine line: mmspoLines){
            MAWMPOLine poLine=new MAWMPOLine();
            LineExtended lineExtended=new LineExtended();
            //setting line values
            poLine.setPurchaseOrderId(mmspo.getPurchaseOrderId());
            poLine.setCanceled(false); //setting default value
            poLine.setClosed(false);    //setting default value
            poLine.setItemId(line.getItemId());
            poLine.setPurchaseOrderLineId(line.getPurchaseOrderLineId());
            poLine.setOrderQuantity(line.getOrderQuantity());
            poLine.setQuantityUomId(line.getQuantityUomId());
            lineExtended.setPutawayType(line.getPutawayType());
            poLine.setExtended(lineExtended);
            mawmpoLines.add(poLine);
        }
        mawmpo.setPurchaseOrderLine(mawmpoLines);
        return mawmpo;
    }
}
