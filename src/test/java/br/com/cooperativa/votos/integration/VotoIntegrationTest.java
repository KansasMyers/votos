package br.com.cooperativa.votos.integration;

import br.com.cooperativa.votos.domain.VotoEnum;
import br.com.cooperativa.votos.dto.PautaRequest;
import br.com.cooperativa.votos.dto.SessaoRequest;
import br.com.cooperativa.votos.dto.VotoRequest;
import br.com.cooperativa.votos.repository.SessaoRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class VotoIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SessaoRepository sessaoRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deveExecutarFluxoCompletoDeVotacao() throws Exception {
        // Criar Pauta
        var pautaReq = new PautaRequest("Pauta Integração", "Descrição");
        String pautaJson = mockMvc.perform(post("/v1/pautas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pautaReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        
        Long pautaId = objectMapper.readTree(pautaJson).get("id").asLong();

        // Abrir Sessão
        var sessaoReq = new SessaoRequest(pautaId, 1);
        mockMvc.perform(post("/v1/pautas/abrir-sessao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sessaoReq)))
                .andExpect(status().isOk());

        // Votar
        var votoReq = new VotoRequest("assoc-int-1", VotoEnum.SIM);
        mockMvc.perform(post("/v1/pautas/" + pautaId + "/votar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(votoReq)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar 409 Conflict ao tentar votar duas vezes")
    void deveRetornarErroVotoDuplicado() throws Exception {
        // Criar pauta e abrir sessão
        Long pautaId = criarPautaEAbrirSessao("Pauta Duplicidade", 5);

        var votoReq = new VotoRequest("associado-fiel", VotoEnum.SIM);

        // Primeiro voto: OK
        mockMvc.perform(post("/v1/pautas/" + pautaId + "/votar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(votoReq)))
                .andExpect(status().isOk());

        // Segundo voto: Erro (Conflict)
        mockMvc.perform(post("/v1/pautas/" + pautaId + "/votar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(votoReq)))
                .andExpect(status().isConflict()); // Validando nosso ExceptionHandler
    }

    @Test
    @DisplayName("Deve retornar 409 Conflict ao votar em sessão expirada")
    void deveRetornarErroSessaoExpirada() throws Exception {
        // Criar pauta e sessão normal (1 min)
        Long pautaId = criarPautaEAbrirSessao("Pauta Expirada", 1); 

        // Forçar o fechamento da sessão no banco de dados
        var sessao = sessaoRepository.findByPautaId(pautaId).orElseThrow();
        sessao.setDataFechamento(java.time.LocalDateTime.now().minusSeconds(1));
        sessaoRepository.saveAndFlush(sessao); // Garante a persistência imediata

        var votoReq = new VotoRequest("associado-atrasado", VotoEnum.NAO);

        // Deve retornar 409 devido à lógica de 'estaAberta()'
        mockMvc.perform(post("/v1/pautas/" + pautaId + "/votar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(votoReq)))
                .andExpect(status().isConflict());
    }

    private Long criarPautaEAbrirSessao(String titulo, int minutos) throws Exception {
        var pautaReq = new PautaRequest(titulo, "Desc");

        String pautaJson = mockMvc.perform(post("/v1/pautas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pautaReq)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(pautaJson).get("id").asLong();

        var sessaoReq = new SessaoRequest(id, minutos);
        
        mockMvc.perform(post("/v1/pautas/abrir-sessao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sessaoReq)));
        
        return id;
    }

}