package com.buscacep.santander.service;

import com.buscacep.santander.dto.BuscaCEPRespostaAPIDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service("wiremockBuscaClient")
public class WiremockBuscaClient implements CEPApiClient{

    private final RestClient restClient;

    public WiremockBuscaClient(RestClient.Builder builder, @Value("${buscacep.api.url}") String apiUrl) {
        this.restClient = builder.baseUrl(apiUrl).build();
    }

    @Override
    public BuscaCEPRespostaAPIDTO buscarCep(String cep) {

        return restClient.get()
                .uri("/cep?cep={cep}", cep)
                .retrieve()
                .body(BuscaCEPRespostaAPIDTO.class);
    }
}
