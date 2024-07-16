package com.floordecor.inbound.repository;

import com.floordecor.inbound.entity.TransactionStatus;
import com.floordecor.inbound.entity.transaction.InboundTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface InboundTransactionRepository extends JpaRepository<InboundTransaction, String> {

    boolean existsByFileNameAndStatusNot(String fileName, TransactionStatus status);

    @Query(
            "select i from InboundTransaction i where i.status = ?1 and (i.id = ?2 or i.fileName = ?3)")
    Optional<InboundTransaction> findByStatusAndIdOrFileName(
            TransactionStatus status, String id, String fileName);

    @Transactional
    @Modifying
    @Query(
            value =
                    "delete from inb_transaction_status s where interface_type in (?1) and DATEDIFF( CURDATE(), s.last_updated_timestamp ) >= ?2",
            nativeQuery = true)
    int deleteByLastUpdatedTimestampLessThanEqual(List<String> interfaceTypes, int days);
}
