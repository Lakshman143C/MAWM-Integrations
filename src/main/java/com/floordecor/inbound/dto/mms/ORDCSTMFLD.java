package com.floordecor.inbound.dto.mms;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class ORDCSTMFLD {

    private String fieldName;
    private String fieldValue;

}
