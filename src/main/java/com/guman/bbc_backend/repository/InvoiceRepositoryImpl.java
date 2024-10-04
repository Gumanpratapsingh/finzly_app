package com.guman.bbc_backend.repository;

import com.guman.bbc_backend.entity.Invoice;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

public class InvoiceRepositoryImpl implements InvoiceRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void refresh(Invoice invoice) {
        entityManager.refresh(invoice);
    }
}