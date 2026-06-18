package com.buscacep.santander.service;

import com.buscacep.santander.dto.BuscaCEPLogDTO;
import com.buscacep.santander.dto.BuscaCEPRespostaAPIDTO;
import com.buscacep.santander.dto.BuscaCEPRespostaDTO;
import com.buscacep.santander.entity.BuscaCEPLog;
import com.buscacep.santander.entity.BuscaCEPResposta;
import com.buscacep.santander.repository.BuscaCEPLogRepository;
import com.buscacep.santander.repository.BuscaCEPRespostaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuscaCEPServiceTest {

    @Mock
    private BuscaCEPLogRepository logRepository;

    @Mock
    private BuscaCEPRespostaRepository respostaRepository;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private BuscaCEPService buscaCEPService;

    private final String API_URL = "http://localhost:8080";

    @BeforeEach
    void setUp() {

        when(restClientBuilder.baseUrl(API_URL)).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);


        buscaCEPService = new BuscaCEPService(logRepository, respostaRepository, restClientBuilder, API_URL);
        

        lenient().when(restClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void buscarCep_shouldReturnFromDatabase_whenCepExists() {
        // Given
        String cep = "12345-678";
        BuscaCEPResposta cepResposta = createBuscaCEPResposta(cep);
        when(respostaRepository.findByCep(cep)).thenReturn(Optional.of(cepResposta));
        when(logRepository.save(any(BuscaCEPLog.class))).thenReturn(new BuscaCEPLog());

        // When
        BuscaCEPRespostaDTO result = buscaCEPService.buscarCep(cep);

        // Then
        assertNotNull(result);
        assertEquals(cep, result.getCep());
        assertEquals(cepResposta.getId().toString(), result.getId());
        verify(respostaRepository, times(1)).findByCep(cep);
        verify(logRepository, times(1)).save(any(BuscaCEPLog.class));
        verifyNoMoreInteractions(restClient);
    }

    @Test
    void buscarCep_shouldReturnFromExternalApi_whenCepDoesNotExistInDb() {
        // Given
        String cep = "12345-678";
        BuscaCEPRespostaDTO apiRespostaDTO = createBuscaCEPRespostaDTO(cep);
        BuscaCEPRespostaAPIDTO apiResponseWrapper = new BuscaCEPRespostaAPIDTO();
        apiResponseWrapper.setResposta(apiRespostaDTO);

        when(respostaRepository.findByCep(cep)).thenReturn(Optional.empty());
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), eq(cep))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(BuscaCEPRespostaAPIDTO.class)).thenReturn(apiResponseWrapper);

        BuscaCEPResposta savedCepResposta = createBuscaCEPResposta(cep);
        when(respostaRepository.save(any(BuscaCEPResposta.class))).thenReturn(savedCepResposta);
        when(logRepository.save(any(BuscaCEPLog.class))).thenReturn(new BuscaCEPLog());

        // When
        BuscaCEPRespostaDTO result = buscaCEPService.buscarCep(cep);

        // Then
        assertNotNull(result);
        assertEquals(cep, result.getCep());
        assertEquals(savedCepResposta.getId().toString(), result.getId());
        verify(respostaRepository, times(1)).findByCep(cep);
        verify(restClient, times(1)).get();
        verify(responseSpec, times(1)).body(BuscaCEPRespostaAPIDTO.class);
        verify(respostaRepository, times(1)).save(any(BuscaCEPResposta.class));
        verify(logRepository, times(1)).save(any(BuscaCEPLog.class));
    }

    @Test
    void buscarCep_shouldThrowException_whenExternalApiReturnsNullResponse() {
        // Given
        String cep = "12345-678";
        when(respostaRepository.findByCep(cep)).thenReturn(Optional.empty());
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), eq(cep))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(BuscaCEPRespostaAPIDTO.class)).thenReturn(null); // API returns null

        // When / Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> buscaCEPService.buscarCep(cep));
        assertEquals("CEP não encontrado: " + cep, exception.getMessage());
        verify(respostaRepository, times(1)).findByCep(cep);
        verify(restClient, times(1)).get();
        verify(responseSpec, times(1)).body(BuscaCEPRespostaAPIDTO.class);
        verify(respostaRepository, never()).save(any(BuscaCEPResposta.class));
        verify(logRepository, never()).save(any(BuscaCEPLog.class));
    }

    @Test
    void buscarCep_shouldThrowException_whenExternalApiCallFails() {
        // Given
        String cep = "12345-678";
        when(respostaRepository.findByCep(cep)).thenReturn(Optional.empty());
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), eq(cep))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(BuscaCEPRespostaAPIDTO.class)).thenThrow(new RuntimeException("API error"));

        // When / Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> buscaCEPService.buscarCep(cep));
        assertTrue(exception.getMessage().contains("Erro ao consultar CEP: API error"));
        verify(respostaRepository, times(1)).findByCep(cep);
        verify(restClient, times(1)).get();
        verify(responseSpec, times(1)).body(BuscaCEPRespostaAPIDTO.class);
        verify(respostaRepository, never()).save(any(BuscaCEPResposta.class));
        verify(logRepository, never()).save(any(BuscaCEPLog.class));
    }

    @Test
    void buscaCEPLogDTOList_shouldReturnListOfLogs() {
        // Given
        BuscaCEPResposta cepResposta = createBuscaCEPResposta("12345-678");
        BuscaCEPLog log1 = createBuscaCEPLog("12345-678", cepResposta, BuscaCEPLog.BuscaCEPOrigem.BANCO_DE_DADOS);
        BuscaCEPLog log2 = createBuscaCEPLog("87654-321", cepResposta, BuscaCEPLog.BuscaCEPOrigem.API_EXTERNA);
        List<BuscaCEPLog> logs = List.of(log1, log2);

        when(logRepository.findAll()).thenReturn(logs);

        // When
        List<BuscaCEPLogDTO> result = buscaCEPService.buscaCEPLogDTOList();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(log1.getCep(), result.get(0).getCep());
        assertEquals(log2.getCep(), result.get(1).getCep());
        verify(logRepository, times(1)).findAll();
    }

    private BuscaCEPResposta createBuscaCEPResposta(String cep) {
        BuscaCEPResposta resposta = new BuscaCEPResposta();
        resposta.setId(UUID.randomUUID());
        resposta.setCep(cep);
        resposta.setUf("SP");
        resposta.setCidade("Sao Paulo");
        resposta.setLogradouro("Rua Teste");
        resposta.setBairro("Bairro Teste");
        return resposta;
    }

    private BuscaCEPRespostaDTO createBuscaCEPRespostaDTO(String cep) {
        return BuscaCEPRespostaDTO.builder()
                .id(UUID.randomUUID().toString())
                .cep(cep)
                .uf("SP")
                .cidade("Sao Paulo")
                .logradouro("Rua Teste API")
                .bairro("Bairro Teste API")
                .build();
    }

    private BuscaCEPLog createBuscaCEPLog(String cep, BuscaCEPResposta resposta, BuscaCEPLog.BuscaCEPOrigem origem) {
        BuscaCEPLog log = new BuscaCEPLog();
        log.setId(UUID.randomUUID());
        log.setCep(cep);
        log.setDataConsulta(Instant.now());
        log.setOrigemBusca(origem);
        log.setBuscaCEPResposta(resposta);
        return log;
    }
}