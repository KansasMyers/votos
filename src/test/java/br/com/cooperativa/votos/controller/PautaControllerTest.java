package br.com.cooperativa.votos.controller;

import br.com.cooperativa.votos.domain.Pauta;
import br.com.cooperativa.votos.dto.PautaRequest;
import br.com.cooperativa.votos.service.PautaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PautaController.class)
class PautaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PautaService pautaService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Deve criar uma pauta com sucesso e retornar 201")
    void deveCriarPautaComSucesso() throws Exception {
        var request = new PautaRequest("Nova Pauta", "Descricao");
        var pauta = Pauta.builder().id(1L).titulo("Nova Pauta").build();

        when(pautaService.criarPauta(any(PautaRequest.class))).thenReturn(pauta);

        mockMvc.perform(post("/v1/pautas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.titulo").value("Nova Pauta"));
    }

    @Test
    @DisplayName("Deve retornar 400 ao tentar criar pauta com título em branco")
    void deveRetornarErroValidacao() throws Exception {
        var request = new PautaRequest("", "Descricao"); // Título vazio

        mockMvc.perform(post("/v1/pautas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

}