package br.com.terraverde.soa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.terraverde.soa.model.Endereco;

import java.util.List;

/**
 * Repositório JPA para a entidade Endereco.
 */
@Repository
public interface EnderecoRepository extends JpaRepository<Endereco, Long> {

    // Lista todos os endereços de um parceiro específico
    List<Endereco> findByParceiroId(Long parceiroId);

    // Verifica duplicata: mesmo CEP e número para o mesmo parceiro
    boolean existsByCepAndNumeroAndParceiroId(String cep, String numero, Long parceiroId);
}