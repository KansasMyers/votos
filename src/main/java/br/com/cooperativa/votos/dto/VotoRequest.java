package br.com.cooperativa.votos.dto;

import br.com.cooperativa.votos.domain.VotoEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VotoRequest(
    
    @NotBlank(message = "ID do associado é obrigatório")
    String associadoId,
    
    @NotNull(message = "O voto (SIM/NAO) é obrigatório")
    VotoEnum voto

) {}