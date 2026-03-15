package br.com.cooperativa.votos.dto;

public record ResultadoResponse(
    Long pautaId,
    String titulo,
    long votosSim,
    long votosNao,
    String resultado
) {}