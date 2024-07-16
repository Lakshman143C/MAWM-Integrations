package com.floordecor.inbound.dto.mms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class MMSAsn {
    @NotBlank
    @NotNull
    private String asnId;
    @Builder.Default
    private boolean canceled=false;
    private String asnOriginTypeId;
    private String destinationFacilityId;
    private Date estimatedDeliveryDate;
    private String originFacilityId;
    private String vendorId;
    private String transferNumber;
    private String containerNumber;
    private Set<ASNLine> asnLines = new HashSet<>();
    public void addReference() {
        if (!asnLines.isEmpty()) {
            for (ASNLine asnLine : asnLines) {
                asnLine.setAsn(this);
            }
        }
    }
}
