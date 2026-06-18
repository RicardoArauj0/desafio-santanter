package com.buscacep.santander.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BuscaCepDTO {
    @NotBlank(message = "O CEP não pode estar em branco")
    private String cep;
}
