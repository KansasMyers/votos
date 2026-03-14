package br.com.cooperativa.votos.repository;

import br.com.cooperativa.votos.domain.Sessao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SessaoRepository extends JpaRepository<Sessao, Long> {

    Optional<Sessao> findByPautaId(Long pautaId);

}