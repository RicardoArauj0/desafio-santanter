package com.buscacep.santander.service;

import com.buscacep.santander.dto.BuscaCEPRespostaAPIDTO;
import jdk.jshell.spi.ExecutionControl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OtherBuscaClient implements CEPApiClient{

    private final RestClient restClient;

    public OtherBuscaClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public BuscaCEPRespostaAPIDTO buscarCep(String cep) {
        //TODO Busca não implementada.
        throw new UnsupportedOperationException("Payment feature is not implemented yet.");
    }
}
