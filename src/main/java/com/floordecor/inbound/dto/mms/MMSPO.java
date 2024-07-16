package com.floordecor.inbound.dto.mms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class MMSPO {

    @JsonProperty("PurchaseOrderId")
    @NotNull
    private String purchaseOrderId;

    @JsonProperty("VendorId")
    private String vendorId;

    @JsonProperty("OriginFacilityId")
    private String originFacilityId;

    @JsonProperty("DeliveryStartDate")
    private String deliveryStartDate;

    @JsonProperty("DeliveryEndDate")
    private String deliveryEndDate;

    @JsonProperty("Closed")
    private boolean closed;

    @JsonProperty("Canceled")
    private boolean canceled;

    @JsonProperty("DestinationFacilityId")
    private String destinationFacilityId;

    @JsonProperty("Extended")
    private List<ORDCSTMFLD> extended=new ArrayList<>();;

    @JsonProperty("PurchaseOrderLine")
    private List<MMSPOLine> purchaseOrderLine=new ArrayList<>();

    public void setPurchaseOrderId(String purchaseOrderId) {
        if (purchaseOrderId != null && purchaseOrderId.isEmpty()) {
            this.purchaseOrderId = null;
        } else {
            this.purchaseOrderId = purchaseOrderId;
        }
    }

    public @NotNull String getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getOriginFacilityId() {
        return originFacilityId;
    }

    public void setOriginFacilityId(String originFacilityId) {
        this.originFacilityId = originFacilityId;
    }

    public String getDeliveryStartDate() {
        return deliveryStartDate;
    }

    public void setDeliveryStartDate(String deliveryStartDate) {
        this.deliveryStartDate = deliveryStartDate;
    }

    public String getDeliveryEndDate() {
        return deliveryEndDate;
    }

    public void setDeliveryEndDate(String deliveryEndDate) {
        this.deliveryEndDate = deliveryEndDate;
    }

    public boolean getClosed() {
        return closed;
    }

    public void setClosed(String closed) {
        if(closed.equalsIgnoreCase("false"))
            this.closed = false;
        else this.closed=true;
    }

    public boolean getCanceled() {
        return canceled;
    }

    public void setCanceled(String canceled) {
        if(canceled.equalsIgnoreCase("false"))
            this.canceled = false;
        else this.canceled=true;
    }

    public String getDestinationFacilityId() {
        return destinationFacilityId;
    }

    public void setDestinationFacilityId(String destinationFacilityId) {
        this.destinationFacilityId = destinationFacilityId;
    }

    public List<ORDCSTMFLD> getExtended() {
        return extended;
    }

    public void setExtended(List<ORDCSTMFLD> extended) {
        this.extended = extended;
    }

    public List<MMSPOLine> getPurchaseOrderLine() {
        return purchaseOrderLine;
    }

    public void setPurchaseOrderLine(List<MMSPOLine> purchaseOrderLine) {
        this.purchaseOrderLine = purchaseOrderLine;
    }
}
