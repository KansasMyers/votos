package br.com.cooperativa.votos.service;

import br.com.cooperativa.votos.domain.Voto;
import br.com.cooperativa.votos.dto.ResultadoResponse;
import br.com.cooperativa.votos.dto.VotoRequest;
import br.com.cooperativa.votos.repository.PautaRepository;
import br.com.cooperativa.votos.repository.SessaoRepository;
import br.com.cooperativa.votos.repository.VotoRepository;
import br.com.cooperativa.votos.domain.VotoEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VotoService {

    private final VotoRepository votoRepository;
    private final SessaoRepository sessaoRepository;
    private final PautaRepository pautaRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public void votar(Long pautaId, VotoRequest request) {
        var sessao = sessaoRepository.findByPautaId(pautaId)
                .orElseThrow(() -> new IllegalArgumentException("Sessão não encontrada para esta pauta"));

        if (!sessao.estaAberta()) {
            throw new IllegalStateException("A sessão de votação já está fechada");
        }

        if (votoRepository.existsByPautaIdAndAssociadoId(pautaId, request.associadoId())) {
            throw new IllegalStateException("Associado já votou nesta pauta");
        }

        var voto = Voto.builder()
                .pauta(sessao.getPauta())
                .associadoId(request.associadoId())
                .voto(request.voto())
                .build();

        votoRepository.save(voto);
        log.info("Voto registrado com sucesso para o associado: {}", request.associadoId());
    }

    @Transactional(readOnly = true)
    public ResultadoResponse contabilizar(Long pautaId) {
        var pauta = pautaRepository.findById(pautaId)
                .orElseThrow(() -> new IllegalArgumentException("Pauta não encontrada"));

        long sim = votoRepository.countByPautaIdAndVoto(pautaId, VotoEnum.SIM);
        long nao = votoRepository.countByPautaIdAndVoto(pautaId, VotoEnum.NAO);

        String status = (sim > nao) ? "APROVADO" : (nao > sim) ? "REPROVADO" : "EMPATE";

        var resultado = new ResultadoResponse(pautaId, pauta.getTitulo(), sim, nao, status);

        // Notificar via Mensageria
        kafkaTemplate.send("pauta-votacao-resultado", pautaId.toString(), resultado);

        return resultado;
    }

}