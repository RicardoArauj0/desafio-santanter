package com.buscacep.santander.service;

import com.buscacep.santander.dto.BuscaCEPLogDTO;
import com.buscacep.santander.dto.BuscaCEPRespostaAPIDTO;
import com.buscacep.santander.dto.BuscaCEPRespostaDTO;
import com.buscacep.santander.entity.BuscaCEPLog;
import com.buscacep.santander.entity.BuscaCEPResposta;
import com.buscacep.santander.repository.BuscaCEPLogRepository;
import com.buscacep.santander.repository.BuscaCEPRespostaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuscaCEPServiceTest {

    @Mock
    private BuscaCEPLogRepository logRepository;

    @Mock
    private BuscaCEPRespostaRepository respostaRepository;

    @Mock
    private CEPApiClient cepApiClient;

    @InjectMocks
    private BuscaCEPService buscaCEPService;

    private String cepValido;
    private BuscaCEPResposta buscaCEPRespostaMock;
    private UUID idMock;

    @BeforeEach
    void setUp() {
        cepValido = "01001000";
        idMock = UUID.randomUUID();

        buscaCEPRespostaMock = new BuscaCEPResposta();
        buscaCEPRespostaMock.setId(idMock);
        buscaCEPRespostaMock.setCep(cepValido);
        buscaCEPRespostaMock.setUf("SP");
        buscaCEPRespostaMock.setCidade("São Paulo");
        buscaCEPRespostaMock.setLogradouro("Praça da Sé");
        buscaCEPRespostaMock.setBairro("Sé");
    }

    @Test
    @DisplayName("Deve retornar CEP do Banco de Dados quando já estiver cadastrado")
    void buscarCep_DeveRetornarDoBancoDeDados_QuandoCepExistir() {
        // Arrange
        when(respostaRepository.findByCep(cepValido)).thenReturn(Optional.of(buscaCEPRespostaMock));

        // Act
        BuscaCEPRespostaDTO resultado = buscaCEPService.buscarCep(cepValido);

        // Assert
        assertNotNull(resultado);
        assertEquals(cepValido, resultado.getCep());
        assertEquals("SP", resultado.getUf());
        assertEquals(idMock.toString(), resultado.getId());

        // Verifica se salvou o log histórico com a origem correta (BANCO_DE_DADOS)
        ArgumentCaptor<BuscaCEPLog> logCaptor = ArgumentCaptor.forClass(BuscaCEPLog.class);
        verify(logRepository, times(1)).save(logCaptor.capture());
        assertEquals(BuscaCEPLog.BuscaCEPOrigem.BANCO_DE_DADOS, logCaptor.getValue().getOrigemBusca());

        // Garante que a API externa NÃO foi chamada
        verifyNoInteractions(cepApiClient);
    }

    @Test
    @DisplayName("Deve buscar CEP na API externa e salvar no banco quando não encontrar localmente")
    void buscarCep_DeveBuscarNaApiESalvar_QuandoCepNaoExistirNoBanco() {
        // Arrange
        when(respostaRepository.findByCep(cepValido)).thenReturn(Optional.empty());

        BuscaCEPRespostaDTO apiRespostaDTO = BuscaCEPRespostaDTO.builder()
                .cep(cepValido)
                .uf("SP")
                .cidade("São Paulo")
                .logradouro("Praça da Sé")
                .bairro("Sé")
                .build();

        BuscaCEPRespostaAPIDTO apiDTOWrapper = new BuscaCEPRespostaAPIDTO();
        apiDTOWrapper.setResposta(apiRespostaDTO);

        when(cepApiClient.buscarCep(cepValido)).thenReturn(apiDTOWrapper);
        when(respostaRepository.save(any(BuscaCEPResposta.class))).thenReturn(buscaCEPRespostaMock);

        // Act
        BuscaCEPRespostaDTO resultado = buscaCEPService.buscarCep(cepValido);

        // Assert
        assertNotNull(resultado);
        assertEquals(idMock.toString(), resultado.getId());
        verify(respostaRepository, times(1)).save(any(BuscaCEPResposta.class));

        // Verifica se salvou o log com a origem correta (API_EXTERNA)
        ArgumentCaptor<BuscaCEPLog> logCaptor = ArgumentCaptor.forClass(BuscaCEPLog.class);
        verify(logRepository, times(1)).save(logCaptor.capture());
        assertEquals(BuscaCEPLog.BuscaCEPOrigem.API_EXTERNA, logCaptor.getValue().getOrigemBusca());
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando a API externa retornar null ou vazia")
    void buscarCep_DeveLancarException_QuandoApiRetornarRespostaNula() {
        // Arrange
        when(respostaRepository.findByCep(cepValido)).thenReturn(Optional.empty());
        when(cepApiClient.buscarCep(cepValido)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            buscaCEPService.buscarCep(cepValido);
        });

        assertTrue(exception.getMessage().contains("CEP não encontrado"));
        verify(respostaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar RuntimeException contendo o erro original caso a API falhe")
    void buscarCep_DeveLancarException_QuandoApiFalhar() {
        // Arrange
        when(respostaRepository.findByCep(cepValido)).thenReturn(Optional.empty());
        when(cepApiClient.buscarCep(cepValido)).thenThrow(new RuntimeException("Timeout de Conexão"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            buscaCEPService.buscarCep(cepValido);
        });

        assertTrue(exception.getMessage().contains("Erro ao consultar CEP: Timeout de Conexão"));
    }

    @Test
    @DisplayName("Deve retornar uma lista de Logs DTO devidamente mapeados")
    void buscaCEPLogDTOList_DeveRetornarListaDeLogsMapeados() {
        // Arrange
        BuscaCEPLog logEntidade = new BuscaCEPLog();
        logEntidade.setId(UUID.randomUUID());
        logEntidade.setCep(cepValido);
        logEntidade.setDataConsulta(Instant.now());
        logEntidade.setOrigemBusca(BuscaCEPLog.BuscaCEPOrigem.BANCO_DE_DADOS);
        logEntidade.setBuscaCEPResposta(buscaCEPRespostaMock);

        when(logRepository.findAll()).thenReturn(Collections.singletonList(logEntidade));

        // Act
        List<BuscaCEPLogDTO> resultado = buscaCEPService.buscaCEPLogDTOList();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(cepValido, resultado.get(0).getCep());
        assertEquals("BANCO_DE_DADOS", resultado.get(0).getOrigemBusca());
        assertNotNull(resultado.get(0).getBuscaCEPRespostaDTO());
    }
}