package com.guman.bbc_backend.repository;
import com.guman.bbc_backend.repository.InvoiceRepository;
import org.apache.pdfbox.pdmodel.PDDocument;

import com.guman.bbc_backend.entity.ConsumerID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsumerIDRepository extends JpaRepository<ConsumerID, Integer> {
    Optional<ConsumerID> findByUserIdAndConnectionId(Integer userId, String connectionId);
    Optional<ConsumerID> findByConnectionId(String connectionId);
    List<ConsumerID> findByUserId(Integer userId);
    boolean existsByConnectionId(String connectionId);
}