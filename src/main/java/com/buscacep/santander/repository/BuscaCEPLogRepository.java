package com.buscacep.santander.repository;

import com.buscacep.santander.entity.BuscaCEPLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BuscaCEPLogRepository extends JpaRepository<BuscaCEPLog, UUID> {

}
