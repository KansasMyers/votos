package br.com.cooperativa.votos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record VotoRequest(
    
    @NotBlank(message = "ID do associado é obrigatório")
    String associadoId,
    
    @NotNull(message = "O voto é obrigatório")
    @Pattern(regexp = "SIM|NAO", message = "O voto deve ser SIM ou NAO")
    String voto

) {}