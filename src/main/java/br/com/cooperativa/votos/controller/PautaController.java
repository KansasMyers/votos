package br.com.cooperativa.votos.controller;

import br.com.cooperativa.votos.domain.Pauta;
import br.com.cooperativa.votos.domain.Sessao;
import br.com.cooperativa.votos.dto.PautaRequest;
import br.com.cooperativa.votos.dto.SessaoRequest;
import br.com.cooperativa.votos.service.PautaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/pautas")
@RequiredArgsConstructor
@Tag(name = "Pautas", description = "Gerenciamento de pautas e sessões de votação")
public class PautaController {

    private final PautaService pautaService;

    @PostMapping
    @Operation(summary = "Cadastrar uma nova pauta")
    public ResponseEntity<Pauta> criar(@RequestBody @Valid PautaRequest request) {
        Pauta pauta = pautaService.criarPauta(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(pauta);
    }

    @PostMapping("/abrir-sessao")
    @Operation(summary = "Abrir uma sessão de votação em uma pauta")
    public ResponseEntity<Sessao> abrirSessao(@RequestBody @Valid SessaoRequest request) {
        Sessao sessao = pautaService.abrirSessao(request);

        return ResponseEntity.ok(sessao);
    }

}