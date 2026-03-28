package br.com.terraverde.soa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.terraverde.soa.model.Produto;
import br.com.terraverde.soa.repository.ProdutoRepository;

import java.util.List;


@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository repository;

    /**
     * Cadastra um novo produto no catálogo.
     * Valida unicidade do nome para evitar duplicatas.
     *
     * @param produto O produto a ser cadastrado (sem ID).
     * @return O produto persistido com ID gerado.
     * @throws RuntimeException Se já existir um produto com o mesmo nome.
     */
    public Produto cadastrar(Produto produto) {
        if (repository.existsByNomeIgnoreCase(produto.getNome())) {
            throw new RuntimeException("Já existe um produto cadastrado com o nome: " + produto.getNome());
        }

        // Padroniza unidade de medida em maiúsculas (ex: "kg" → "KG")
        if (produto.getUnidadeMedida() != null) {
            produto.setUnidadeMedida(produto.getUnidadeMedida().toUpperCase());
        }

        return repository.save(produto);
    }

    /**
     * Lista todos os produtos do catálogo.
     *
     * @return Lista com todos os produtos cadastrados.
     */
    public List<Produto> listar() {
        return repository.findAll();
    }

    /**
     * Atualiza os dados de um produto existente.
     * Valida se o novo nome já pertence a OUTRO produto antes de salvar.
     *
     * @param produto O produto com os dados atualizados (deve conter ID válido).
     * @throws RuntimeException Se o produto não for encontrado, ou se o nome
     *                          já pertencer a outro produto cadastrado.
     */
    public void atualizar(Produto produto) {
        if (!repository.existsById(produto.getId())) {
            throw new RuntimeException("Produto não encontrado para atualização (ID: " + produto.getId() + ").");
        }

        // Valida se o novo nome já pertence a OUTRO produto (não ao próprio)
        repository.findByNomeIgnoreCase(produto.getNome())
            .filter(existente -> !existente.getId().equals(produto.getId()))
            .ifPresent(conflito -> {
                throw new RuntimeException(
                    "Nome já está em uso pelo produto de ID: " + conflito.getId() + "."
                );
            });

        if (produto.getUnidadeMedida() != null) {
            produto.setUnidadeMedida(produto.getUnidadeMedida().toUpperCase());
        }

        repository.save(produto);
    }

    /**
     * Remove um produto do catálogo pelo ID.
     *
     * @param id O ID do produto a ser removido.
     * @throws RuntimeException Se nenhum produto com o ID fornecido existir.
     */
    public void remover(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Operação cancelada: produto com ID " + id + " não existe.");
        }
        repository.deleteById(id);
    }
}