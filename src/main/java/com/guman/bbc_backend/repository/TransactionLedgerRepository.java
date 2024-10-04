package com.guman.bbc_backend.repository;

import com.guman.bbc_backend.entity.TransactionLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionLedgerRepository extends JpaRepository<TransactionLedger, Integer> {
    List<TransactionLedger> findByConsumerId(Integer consumerId);
    List<TransactionLedger> findByConsumerIdInOrderByTransactionDateDesc(List<Integer> consumerIds);
    // Remove the findByConnectionId method as it's not needed
}