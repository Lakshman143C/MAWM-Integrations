package com.floordecor.inbound.dto.mms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseEntity implements Serializable {
    @Column(updatable = false)
    private Date createdTimestamp;

    private Date lastUpdatedTimestamp;

    private String createdBy;
    private String updatedBy;

    @PrePersist
    private void prePersist() {
        this.createdTimestamp = new Date();
        this.lastUpdatedTimestamp = new Date();
    }

    @PreUpdate
    private void preUpdate() {
        this.lastUpdatedTimestamp = new Date();
    }
}
