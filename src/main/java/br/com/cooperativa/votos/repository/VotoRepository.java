package br.com.cooperativa.votos.repository;

import br.com.cooperativa.votos.domain.Voto;
import br.com.cooperativa.votos.domain.VotoEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VotoRepository extends JpaRepository<Voto, Long> {

    // Para o requisito de contabilização de votos 
    long countByPautaIdAndVoto(Long pautaId, VotoEnum voto);
    
    // Para validar se o associado já votou na pauta
    boolean existsByPautaIdAndAssociadoId(Long pautaId, String associadoId);

}