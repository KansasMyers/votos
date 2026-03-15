package br.com.cooperativa.votos.controller;

import br.com.cooperativa.votos.dto.ResultadoResponse;
import br.com.cooperativa.votos.dto.VotoRequest;
import br.com.cooperativa.votos.service.VotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/pautas/{pautaId}")
@RequiredArgsConstructor
@Tag(name = "Votos", description = "Operações de votação e resultados")
public class VotoController {

    private final VotoService votoService;

    @PostMapping("/votar")
    @Operation(summary = "Receber votos dos associados")
    public ResponseEntity<Void> votar(@PathVariable Long pautaId, @RequestBody @Valid VotoRequest request) {
        votoService.votar(pautaId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/resultado")
    @Operation(summary = "Contabilizar os votos e dar o resultado")
    public ResponseEntity<ResultadoResponse> obterResultado(@PathVariable Long pautaId) {
        return ResponseEntity.ok(votoService.contabilizar(pautaId));
    }

}