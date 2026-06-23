package com.buscacep.santander.service;

import com.buscacep.santander.dto.BuscaCEPLogDTO;
import com.buscacep.santander.dto.BuscaCEPRespostaAPIDTO;
import com.buscacep.santander.dto.BuscaCEPRespostaDTO;
import com.buscacep.santander.entity.BuscaCEPLog;
import com.buscacep.santander.entity.BuscaCEPResposta;
import com.buscacep.santander.repository.BuscaCEPLogRepository;
import com.buscacep.santander.repository.BuscaCEPRespostaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class BuscaCEPService {

    private final BuscaCEPLogRepository logRepository;
    private final BuscaCEPRespostaRepository respostaRepository;
    private final CEPApiClient cepApiClient;

    // Usando o (O)pen close princeiple do SOLID. Aqui estou injetando a interface e se
    // precisarmos mudar a API é só usar outra implementação sem mudar essa classe.
    public BuscaCEPService(BuscaCEPLogRepository logRepository,
                           BuscaCEPRespostaRepository respostaRepository,
                           @Qualifier("wiremockBuscaClient") CEPApiClient cepApiClient) {
        this.logRepository = logRepository;
        this.respostaRepository = respostaRepository;
        this.cepApiClient = cepApiClient;
    }

    @Transactional
    public BuscaCEPRespostaDTO buscarCep(String cep) {
        Optional<BuscaCEPResposta> logOptional = respostaRepository.findByCep(cep);

        if (logOptional.isPresent()) {
            log.info("CEP encontrado no banco de dados: {}", cep);

            BuscaCEPResposta cepResposta = logOptional.get();
            BuscaCEPRespostaDTO respostaDTO = BuscaCEPRespostaDTO.builder()
                    .id(cepResposta.getId().toString())
                    .cep(cepResposta.getCep())
                    .uf(cepResposta.getUf())
                    .cidade(cepResposta.getCidade())
                    .logradouro(cepResposta.getLogradouro())
                    .bairro(cepResposta.getBairro())
                    .build();

            salvarBuscaCEPLog(cep, cepResposta, BuscaCEPLog.BuscaCEPOrigem.BANCO_DE_DADOS);
            return respostaDTO;
        }
        try {
            log.info("Buscando CEP na API externa: {}", cep);
            BuscaCEPRespostaAPIDTO respostaApiDTO = cepApiClient.buscarCep(cep);

            if (respostaApiDTO != null && respostaApiDTO.getResposta() != null) {
                log.info("CEP encontrado na API externa: {}", cep);
                BuscaCEPRespostaDTO respostaDTO = respostaApiDTO.getResposta();
                BuscaCEPResposta buscaCEPResposta = new BuscaCEPResposta();
                buscaCEPResposta.setCep(respostaDTO.getCep());
                buscaCEPResposta.setUf(respostaDTO.getUf());
                buscaCEPResposta.setCidade(respostaDTO.getCidade());
                buscaCEPResposta.setLogradouro(respostaDTO.getLogradouro());
                buscaCEPResposta.setBairro(respostaDTO.getBairro());

                BuscaCEPResposta respostaSalva = respostaRepository.save(buscaCEPResposta);
                salvarBuscaCEPLog(cep, buscaCEPResposta, BuscaCEPLog.BuscaCEPOrigem.API_EXTERNA);

                respostaDTO.setId(respostaSalva.getId().toString());
                return respostaDTO;
            }
        } catch (Exception e) {
            log.error("Erro ao buscar CEP na API externa {}", e.getMessage());
            throw new RuntimeException("Erro ao consultar CEP: " + e.getMessage());
        }
        throw new RuntimeException("CEP não encontrado: " + cep);
    }

    public List<BuscaCEPLogDTO> buscaCEPLogDTOList () {
        return logRepository.findAll().stream().map(this::cepLogToDTO).toList();
    }

    private void salvarBuscaCEPLog(String cep, BuscaCEPResposta cepResposta, BuscaCEPLog.BuscaCEPOrigem origem) {
        BuscaCEPLog log = new BuscaCEPLog();
        log.setCep(cep);
        log.setDataConsulta(java.time.Instant.now());
        log.setOrigemBusca(origem);
        log.setBuscaCEPResposta(cepResposta);
        logRepository.save(log);
    }

    private BuscaCEPLogDTO cepLogToDTO(BuscaCEPLog log) {
        BuscaCEPLogDTO dto = new BuscaCEPLogDTO();
        dto.setId(log.getId().toString());
        dto.setCep(log.getCep());
        dto.setDataConsulta(log.getDataConsulta());
        dto.setOrigemBusca(log.getOrigemBusca().toString());
        dto.setBuscaCEPRespostaDTO(toBuscaCEPRespostaDTO(log.getBuscaCEPResposta()));
        return dto;
    }

    private BuscaCEPRespostaDTO toBuscaCEPRespostaDTO(BuscaCEPResposta resposta) {
        return BuscaCEPRespostaDTO.builder()
                .id(resposta.getId().toString())
                .cep(resposta.getCep())
                .uf(resposta.getUf())
                .bairro(resposta.getBairro())
                .cidade(resposta.getCidade())
                .logradouro(resposta.getLogradouro())
                .build();
    }

}
