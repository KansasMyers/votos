package br.com.cooperativa.votos.service;

import br.com.cooperativa.votos.domain.Sessao;
import br.com.cooperativa.votos.repository.SessaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessaoScheduler {

    private final SessaoRepository sessaoRepository;
    private final VotoService votoService;

    @Scheduled(fixedDelay = 10000) // Executa a cada 10 segundos
    @Transactional
    public void verificarSessoesFinalizadas() {
        log.debug("Iniciando verificação de sessões expiradas...");

        // Seleciona sessões vencidas e não processadas
        List<Sessao> sessoesParaProcessar = sessaoRepository
                .findAllByDataFechamentoBeforeAndProcessadaFalse(LocalDateTime.now());

        for (Sessao sessao : sessoesParaProcessar) {
            try {
                log.info("Processando fechamento da pauta: {}", sessao.getPauta().getId());

                // Contabiliza e envia para o Kafka
                votoService.contabilizar(sessao.getPauta().getId());

                // Marca como processada para não repetir
                sessao.setProcessada(true);
                sessaoRepository.save(sessao);

            } catch (Exception e) {
                log.error("Erro ao processar sessão da pauta {}: {}",
                        sessao.getPauta().getId(), e.getMessage());
            }
        }
    }

}