package br.com.terraverde.soa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.terraverde.soa.model.Parceiro;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositório JPA para a entidade Parceiro.
 *
 * Adicionados os métodos findByDocumento e findByEmail (retornam Optional),
 * necessários para que o ParceiroService.atualizar() possa verificar se o
 * novo documento/e-mail já pertencem a OUTRO parceiro antes de salvar.
 */
@Repository
public interface ParceiroRepository extends JpaRepository<Parceiro, Long> {

    // Verificações de existência (usadas no criar())
    boolean existsByDocumento(String documento);
    boolean existsByEmail(String email);

    // Buscas por valor único (usadas no atualizar() para validação cruzada)
    Optional<Parceiro> findByDocumento(String documento);
    Optional<Parceiro> findByEmail(String email);

    // Verificação composta (mantida para compatibilidade)
    boolean existsByDocumentoIgnoreCaseOrEmailIgnoreCase(String documento, String email);

    // Listagem e relatórios
    List<Parceiro> findByTipoIgnoreCase(String tipo);
    long countByTipo(String tipo);
    List<Parceiro> findTop10ByOrderByDataHoraCadastroDesc();
    List<Parceiro> findByDataHoraCadastroAfter(LocalDateTime data);
}