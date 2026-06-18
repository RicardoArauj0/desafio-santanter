package com.buscacep.santander.repository;


import com.buscacep.santander.entity.BuscaCEPResposta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BuscaCEPRespostaRepository extends JpaRepository<BuscaCEPResposta, UUID> {
    Optional<BuscaCEPResposta> findByCep(String cep);
}
