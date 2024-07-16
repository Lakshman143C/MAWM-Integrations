package com.floordecor.inbound.entity.transaction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.floordecor.inbound.dto.enums.InterfaceTypes;
import com.floordecor.inbound.dto.enums.Source;
import com.floordecor.inbound.entity.TransactionStatus;
import com.floordecor.inbound.dto.mms.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "inb_transaction_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class InboundTransaction extends BaseEntity {

    @Id
    private String id;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "varchar(1000)")
    private String fileName;

    @Enumerated(EnumType.STRING)
    @NotNull
    private InterfaceTypes interfaceType;

    @Builder.Default private int processedCount = 0;

    @Builder.Default private int totalCount = 0;

    @Enumerated(EnumType.STRING)
    @NotNull private Source source;

    @Enumerated(EnumType.STRING)
    @NotNull @Builder.Default
    private TransactionStatus status = TransactionStatus.STARTED;
}
