package com.buscacep.santander.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BuscaCEPRespostaAPIDTO {
    private String status;
    @JsonProperty("body")
    private BuscaCEPRespostaDTO resposta;
}


