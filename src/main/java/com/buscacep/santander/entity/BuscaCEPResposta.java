package com.buscacep.santander.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.util.UUID;

@Getter
@Setter
@Entity()
@Table(name = "busca_cep_resposta")
public class BuscaCEPResposta {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "CHAR(36)", nullable = false)
    @JdbcTypeCode(Types.VARCHAR)
    private UUID id;
    @Column(unique = true)
    private String cep;
    private String uf;
    private String cidade;
    private String logradouro;
    private String bairro;

}
