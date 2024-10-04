package com.guman.bbc_backend.repository;

import com.guman.bbc_backend.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer>, InvoiceRepositoryCustom {

    List<Invoice> findByConsumerId(Integer consumerId);

    List<Invoice> findByConsumerIdAndStatus(Integer consumerId, Invoice.InvoiceStatus status);

    List<Invoice> findByBillDueDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT i FROM Invoice i WHERE i.consumerId = :consumerId ORDER BY i.billingEndDate DESC")
    Invoice findLatestByConsumerId(@Param("consumerId") Integer consumerId);

    Optional<Invoice> findByConsumerIdAndBillingStartDateAndBillingEndDate(Integer consumerId, LocalDate startDate, LocalDate endDate);

    Optional<Invoice> findTopByConsumerIdAndStatusOrderByBillingEndDateDesc(Integer consumerId, Invoice.InvoiceStatus status);
}
