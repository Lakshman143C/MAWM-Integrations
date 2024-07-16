package com.floordecor.inbound.dto.mms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class ASNLine {
    private MMSAsn asn;
    @NotNull @NotBlank private String asnLineId;
    private String batchNumber;
    @Builder.Default
    private boolean canceled=false;
    private String inventoryAttribute1;
    private String inventoryAttribute2;
    @NotNull @NotBlank private String itemId;
    @Builder.Default
    private String purchaseOrderId="TRANSFER";
    private String purchaseOrderLineId;
    private String qtyUomId;
    @NotNull private int shippedQty;

    public void setShippedQty(double shippedQty) {
        this.shippedQty = (int) Math.round(shippedQty);
    }
}
