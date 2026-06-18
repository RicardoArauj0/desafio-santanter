package com.buscacep.santander.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class BuscaCEPLogDTO {
    private String id;
    private String cep;
    private Instant dataConsulta;
    private String origemBusca;
    private BuscaCEPRespostaDTO buscaCEPRespostaDTO;
}
