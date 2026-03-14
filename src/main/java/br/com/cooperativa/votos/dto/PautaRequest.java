package br.com.cooperativa.votos.dto;

import jakarta.validation.constraints.NotBlank;

public record PautaRequest(
    @NotBlank(message = "O título é obrigatório") 
    String titulo, 
    String descricao
) {}