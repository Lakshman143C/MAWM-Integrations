package com.floordecor.inbound.dto.mms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class MMSPOLine {
    @JsonProperty("PurchaseOrderId")
    private String purchaseOrder;

    @JsonProperty("PurchaseOrderLineId")
    private String purchaseOrderLineId;

    @NotBlank
    @NotNull
    @JsonProperty("ItemId")
    private String itemId;

    @NotBlank
    @NotNull
    @JsonProperty("OrderQuantity")
    private int orderQuantity;

    @JsonProperty("QuantityUomId")
    private String quantityUomId;

    @JsonProperty("Closed")
    private String closed;

    @JsonProperty("Canceled")
    private String canceled;

    @JsonProperty("PutawayType")
    private String putawayType;

    public void setItemId(String itemId) {
        if (itemId != null && itemId.isEmpty()) {
            this.itemId = null;
        } else {
            this.itemId = itemId;
        }
    }

    public void setOrderQuantity(String orderQuantity) {
        double parsedOrderQuantity;
        try {
            parsedOrderQuantity = Double.parseDouble(orderQuantity);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Order quantity must be a number.");
        }

        // Check if the number is a whole number
        if (parsedOrderQuantity % 1 != 0) {
            throw new IllegalArgumentException("Order quantity must be a Integer.");
        }

        this.orderQuantity = (int) parsedOrderQuantity;
    }
    public void setPurchaseItemId(String itemId) {
        if (itemId != null && itemId.isEmpty()) {
            this.itemId = null;
        } else {
            this.itemId = itemId;
        }
    }
}

