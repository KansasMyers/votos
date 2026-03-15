package br.com.cooperativa.votos.performance;

import br.com.cooperativa.votos.domain.VotoEnum;
import br.com.cooperativa.votos.dto.PautaRequest;
import br.com.cooperativa.votos.dto.SessaoRequest;
import br.com.cooperativa.votos.dto.VotoRequest;
import br.com.cooperativa.votos.integration.AbstractIntegrationTest;
import br.com.cooperativa.votos.repository.VotoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VotoStressPerformanceTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private VotoRepository votoRepository;

    private static final int TOTAL_VOTOS = 1000; // Simulação de carga rápida

    @Test
    @DisplayName("Deve processar alta carga de votos simultâneos com Virtual Threads")
    void deveSuportarAltaCargaDeVotos() throws Exception {
        // Criar pauta e abrir sessão
        Long pautaId = setupPauta();

        // ExecutorService para simular concorrência real
        // Com a flag spring.threads.virtual.enabled=true, isso usará Virtual Threads
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CountDownLatch latch = new CountDownLatch(TOTAL_VOTOS);
            AtomicInteger sucessos = new AtomicInteger();
            AtomicInteger falhas = new AtomicInteger();

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < TOTAL_VOTOS; i++) {
                String associadoId = UUID.randomUUID().toString();
                executor.submit(() -> {
                    try {
                        var request = new VotoRequest(associadoId, VotoEnum.SIM);
                        mockMvc.perform(post("/v1/pautas/" + pautaId + "/votar")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk());
                        sucessos.incrementAndGet();
                    } catch (Exception e) {
                        falhas.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            long endTime = System.currentTimeMillis();

            System.out.println("--- RESULTADO DO TESTE DE ESTRESSE ---");
            System.out.println("Tempo total: " + (endTime - startTime) + "ms");
            System.out.println("Votos com sucesso: " + sucessos.get());
            System.out.println("Falhas: " + falhas.get());
            System.out.println("---------------------------------------");

            assertEquals(TOTAL_VOTOS, sucessos.get(), "Todos os votos únicos deveriam ter sido processados");
            assertEquals(TOTAL_VOTOS, votoRepository.countByPautaIdAndVoto(pautaId, VotoEnum.SIM));
        }
    }

    private Long setupPauta() throws Exception {
        var pautaReq = new PautaRequest("Pauta Estresse", "Descrição");

        String pautaJson = mockMvc.perform(post("/v1/pautas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pautaReq)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(pautaJson).get("id").asLong();

        var sessaoReq = new SessaoRequest(id, 10);

        mockMvc.perform(post("/v1/pautas/abrir-sessao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sessaoReq)));

        return id;
    }

}