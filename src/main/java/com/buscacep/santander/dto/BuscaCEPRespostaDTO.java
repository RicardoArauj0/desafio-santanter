package com.buscacep.santander.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuscaCEPRespostaDTO {
    private String id;
    private String cep;
    private String uf;
    private String cidade;
    private String logradouro;
    private String bairro;
}
