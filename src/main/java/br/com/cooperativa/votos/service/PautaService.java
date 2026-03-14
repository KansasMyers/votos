package br.com.cooperativa.votos.service;

import br.com.cooperativa.votos.domain.Pauta;
import br.com.cooperativa.votos.domain.Sessao;
import br.com.cooperativa.votos.dto.PautaRequest;
import br.com.cooperativa.votos.dto.SessaoRequest;
import br.com.cooperativa.votos.repository.PautaRepository;
import br.com.cooperativa.votos.repository.SessaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PautaService {

    private final PautaRepository pautaRepository;
    private final SessaoRepository sessaoRepository;

    @Value("${app.votação.tempo-default-minutos:1}")
    private Integer tempoDefault;

    @Transactional
    public Pauta criarPauta(PautaRequest request) {
        log.info("Criando nova pauta: {}", request.titulo());
        
        var pauta = Pauta.builder()
                .titulo(request.titulo())
                .descricao(request.descricao())
                .build();

        return pautaRepository.save(pauta);
    }

    @Transactional
    public Sessao abrirSessao(SessaoRequest request) {
        log.info("Abrindo sessão para a pauta ID: {}", request.pautaId());

        var pauta = pautaRepository.findById(request.pautaId())
                .orElseThrow(() -> new IllegalArgumentException("Pauta não encontrada"));

        // Evita abrir duas sessões para a mesma pauta
        sessaoRepository.findByPautaId(request.pautaId()).ifPresent(s -> {
            throw new IllegalStateException("Já existe uma sessão para esta pauta");
        });

        int minutos = (request.minutos() != null && request.minutos() > 0) 
                      ? request.minutos() 
                      : tempoDefault;

        var sessao = Sessao.builder()
                .pauta(pauta)
                .dataAbertura(LocalDateTime.now())
                .dataFechamento(LocalDateTime.now().plusMinutes(minutos))
                .build();

        return sessaoRepository.save(sessao);
    }
}