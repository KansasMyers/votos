package br.com.cooperativa.votos.service;

import br.com.cooperativa.votos.domain.Pauta;
import br.com.cooperativa.votos.domain.Sessao;
import br.com.cooperativa.votos.domain.VotoEnum;
import br.com.cooperativa.votos.dto.VotoRequest;
import br.com.cooperativa.votos.repository.PautaRepository;
import br.com.cooperativa.votos.repository.SessaoRepository;
import br.com.cooperativa.votos.repository.VotoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VotoServiceTest {

    @Mock
    private VotoRepository votoRepository;

    @Mock
    private SessaoRepository sessaoRepository;

    @Mock
    private PautaRepository pautaRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private VotoService votoService;

    @Test
    @DisplayName("Deve registar um voto com sucesso")
    void deveVotarComSucesso() {
        Long pautaId = 1L;
        VotoRequest request = new VotoRequest("assoc-1", "SIM");
        Pauta pauta = Pauta.builder().id(pautaId).build();
        Sessao sessao = Sessao.builder()
                .pauta(pauta)
                .dataAbertura(LocalDateTime.now().minusMinutes(1))
                .dataFechamento(LocalDateTime.now().plusMinutes(1))
                .build();

        when(sessaoRepository.findByPautaId(pautaId)).thenReturn(Optional.of(sessao));
        when(votoRepository.existsByPautaIdAndAssociadoId(pautaId, "assoc-1")).thenReturn(false);

        assertDoesNotThrow(() -> votoService.votar(pautaId, request));

        verify(votoRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando a sessão estiver fechada")
    void deveFalharVotoSessaoFechada() {
        Long pautaId = 1L;
        VotoRequest request = new VotoRequest("assoc-1", "SIM");
        Sessao sessao = Sessao.builder()
                .dataAbertura(LocalDateTime.now().minusMinutes(10))
                .dataFechamento(LocalDateTime.now().minusMinutes(5)) // Já fechou
                .build();

        when(sessaoRepository.findByPautaId(pautaId)).thenReturn(Optional.of(sessao));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> votoService.votar(pautaId, request));

        assertEquals("A sessão de votação já está fechada", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção quando associado já votou")
    void deveFalharVotoDuplicado() {
        Long pautaId = 1L;
        VotoRequest request = new VotoRequest("assoc-1", "SIM");
        Sessao sessao = Sessao.builder()
                .dataAbertura(LocalDateTime.now().minusMinutes(1))
                .dataFechamento(LocalDateTime.now().plusMinutes(1))
                .build();

        when(sessaoRepository.findByPautaId(pautaId)).thenReturn(Optional.of(sessao));
        when(votoRepository.existsByPautaIdAndAssociadoId(pautaId, "assoc-1")).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> votoService.votar(pautaId, request));

        assertEquals("Associado já votou nesta pauta", exception.getMessage());
    }

    @Test
    @DisplayName("Deve contabilizar votos e enviar para o Kafka")
    void deveContabilizarEEnviarKafka() {
        Long pautaId = 1L;
        Pauta pauta = Pauta.builder().id(pautaId).titulo("Pauta Teste").build();

        when(pautaRepository.findById(pautaId)).thenReturn(Optional.of(pauta));
        when(votoRepository.countByPautaIdAndVoto(pautaId, VotoEnum.SIM)).thenReturn(10L);
        when(votoRepository.countByPautaIdAndVoto(pautaId, VotoEnum.NAO)).thenReturn(5L);

        var resultado = votoService.contabilizar(pautaId);

        assertEquals(10L, resultado.votosSim());
        assertEquals(5L, resultado.votosNao());
        assertEquals("APROVADO", resultado.resultado());
        verify(kafkaTemplate, times(1)).send(eq("pauta-votacao-resultado"), anyString(), any());
    }

}