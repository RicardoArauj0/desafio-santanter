package com.buscacep.santander.controller;

import com.buscacep.santander.dto.BuscaCEPLogDTO;
import com.buscacep.santander.dto.BuscaCEPRespostaDTO;
import com.buscacep.santander.dto.BuscaCepDTO;
import com.buscacep.santander.service.BuscaCEPService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cep")
public class BuscaCEPController {

    private final BuscaCEPService service;

    public BuscaCEPController(BuscaCEPService service) {
        this.service = service;
    }

    @PostMapping("/busca")
    public ResponseEntity<BuscaCEPRespostaDTO> busca(@RequestBody BuscaCepDTO cep) {
        return ResponseEntity.ok(service.buscarCep(cep.getCep()));
    }

    @GetMapping("/log")
    public ResponseEntity<List<BuscaCEPLogDTO>> getLogs() {
        return ResponseEntity.ok(service.buscaCEPLogDTOList());
    }
}
