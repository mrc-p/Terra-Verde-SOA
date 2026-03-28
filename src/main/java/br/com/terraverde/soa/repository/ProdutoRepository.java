package br.com.terraverde.soa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.terraverde.soa.model.Produto;

import java.util.Optional;

/**
 * Repositório JPA para a entidade Produto.
 */
@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // Verifica unicidade do nome antes de cadastrar
    boolean existsByNomeIgnoreCase(String nome);

    // Busca por nome para validação cruzada no atualizar()
    Optional<Produto> findByNomeIgnoreCase(String nome);
}