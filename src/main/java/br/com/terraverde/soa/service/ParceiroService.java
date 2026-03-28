package br.com.terraverde.soa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.terraverde.soa.model.Parceiro;
import br.com.terraverde.soa.repository.ParceiroRepository;

import java.util.List;


@Service
public class ParceiroService {

    @Autowired
    private ParceiroRepository repository;

    /**
     * Cria um novo parceiro com validações de unicidade de documento e e-mail.
     *
     * @param p O parceiro a ser criado (sem ID).
     * @return O parceiro persistido, com ID gerado.
     * @throws RuntimeException Se o documento ou e-mail já estiverem em uso.
     */
    public Parceiro criar(Parceiro p) {
        if (repository.existsByDocumento(p.getDocumento())) {
            throw new RuntimeException("Documento já cadastrado no sistema.");
        }
        if (repository.existsByEmail(p.getEmail())) {
            throw new RuntimeException("E-mail já está em uso por outro parceiro.");
        }

        // Garante consistência no banco: tipo sempre em maiúsculas
        if (p.getTipo() != null) {
            p.setTipo(p.getTipo().toUpperCase());
        }

        return repository.save(p);
    }

    /**
     * Lista parceiros, permitindo filtrar por tipo ou retornar todos.
     *
     * @param tipo "CLIENTE", "FORNECEDOR", "todos" ou null para retornar todos.
     * @return Lista de parceiros correspondente ao filtro.
     */
    public List<Parceiro> listar(String tipo) {
        if (tipo == null || tipo.isBlank() || tipo.equalsIgnoreCase("todos")) {
            return repository.findAll();
        }
        return repository.findByTipoIgnoreCase(tipo);
    }

    /**
     * Atualiza um parceiro existente com validação completa de unicidade.
     *
     * Verifica se o novo documento ou e-mail pertencem a OUTRO parceiro
     * (diferente do que está sendo atualizado), garantindo a integridade dos dados.
     *
     * @param p O parceiro com os dados atualizados (deve conter ID válido).
     * @throws RuntimeException Se o parceiro não for encontrado, ou se o
     *                          documento/e-mail já pertencer a outro cadastro.
     */
    public void atualizar(Parceiro p) {
        if (!repository.existsById(p.getId())) {
            throw new RuntimeException("Parceiro não encontrado para atualização (ID: " + p.getId() + ").");
        }

        // Verifica se o documento já pertence a OUTRO parceiro (não ao próprio)
        repository.findByDocumento(p.getDocumento())
            .filter(existente -> !existente.getId().equals(p.getId()))
            .ifPresent(conflito -> {
                throw new RuntimeException(
                    "Documento já está em uso pelo parceiro de ID: " + conflito.getId() + "."
                );
            });

        // Verifica se o e-mail já pertence a OUTRO parceiro (não ao próprio)
        repository.findByEmail(p.getEmail())
            .filter(existente -> !existente.getId().equals(p.getId()))
            .ifPresent(conflito -> {
                throw new RuntimeException(
                    "E-mail já está em uso pelo parceiro de ID: " + conflito.getId() + "."
                );
            });

        // Garante consistência no banco: tipo sempre em maiúsculas
        if (p.getTipo() != null) {
            p.setTipo(p.getTipo().toUpperCase());
        }

        repository.save(p);
    }

    /**
     * Remove um parceiro pelo ID.
     *
     * @param id O ID do parceiro a ser removido.
     * @throws RuntimeException Se nenhum parceiro com o ID fornecido existir.
     */
    public void remover(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Operação cancelada: O ID " + id + " não existe.");
        }
        repository.deleteById(id);
    }
}