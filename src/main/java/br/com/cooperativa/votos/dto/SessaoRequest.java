package br.com.cooperativa.votos.dto;

public record SessaoRequest(
    Long pautaId, 
    Integer minutos // Pode ser nulo para usar o default
) {}