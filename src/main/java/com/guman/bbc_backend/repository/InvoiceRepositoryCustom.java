package com.guman.bbc_backend.repository;

import com.guman.bbc_backend.entity.Invoice;

public interface InvoiceRepositoryCustom {
    void refresh(Invoice invoice);
}