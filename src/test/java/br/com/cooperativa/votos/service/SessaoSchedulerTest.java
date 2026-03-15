package br.com.cooperativa.votos.service;

import br.com.cooperativa.votos.domain.Pauta;
import br.com.cooperativa.votos.domain.Sessao;
import br.com.cooperativa.votos.repository.SessaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessaoSchedulerTest {

    @Mock
    private SessaoRepository sessaoRepository;

    @Mock
    private VotoService votoService;

    @InjectMocks
    private SessaoScheduler sessaoScheduler;

    @Test
    @DisplayName("Deve processar sessões expiradas e marcar como processadas")
    void deveProcessarSessoesExpiradas() {
        Pauta pauta = Pauta.builder().id(1L).build();
        Sessao sessao = Sessao.builder().pauta(pauta).processada(false).build();
        
        when(sessaoRepository.findAllByDataFechamentoBeforeAndProcessadaFalse(any()))
                .thenReturn(List.of(sessao));

        sessaoScheduler.verificarSessoesFinalizadas();

        verify(votoService, times(1)).contabilizar(1L);
        verify(sessaoRepository, times(1)).save(sessao);
        assert(sessao.isProcessada());
    }

}