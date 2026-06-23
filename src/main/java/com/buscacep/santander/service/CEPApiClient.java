package com.buscacep.santander.service;

import com.buscacep.santander.dto.BuscaCEPRespostaAPIDTO;

public interface CEPApiClient {

    BuscaCEPRespostaAPIDTO buscarCep(String cep);

}
