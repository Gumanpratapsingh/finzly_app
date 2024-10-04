package com.guman.bbc_backend.repository;

import com.guman.bbc_backend.entity.DueAmount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DueAmountRepository extends JpaRepository<DueAmount, Integer> {

}