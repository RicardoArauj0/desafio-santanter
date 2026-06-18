package com.buscacep.santander.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity()
@Table(name = "busca_cep_log")
public class BuscaCEPLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "CHAR(36)", nullable = false)
    @JdbcTypeCode(Types.VARCHAR)
    private UUID id;
    private String cep;
    private Instant dataConsulta;
    @Enumerated(EnumType.STRING)
    @Column(name = "origem_busca", nullable = false)
    private BuscaCEPOrigem origemBusca;
    @ManyToOne
    @JoinColumn(name = "busca_cep_resposta_id")
    private BuscaCEPResposta buscaCEPResposta;

    public enum BuscaCEPOrigem {
        API_EXTERNA,
        BANCO_DE_DADOS
    }
}