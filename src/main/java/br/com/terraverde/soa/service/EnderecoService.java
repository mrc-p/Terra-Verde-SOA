package br.com.terraverde.soa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.terraverde.soa.model.Endereco;
import br.com.terraverde.soa.model.Parceiro;
import br.com.terraverde.soa.repository.EnderecoRepository;
import br.com.terraverde.soa.repository.ParceiroRepository;

import java.util.List;


@Service
public class EnderecoService {

    @Autowired
    private EnderecoRepository enderecoRepository;

    /**
     * Acesso direto ao repositório de Parceiro apenas para validar existência.
     * Não injeta ParceiroService para manter baixo acoplamento entre serviços.
     */
    @Autowired
    private ParceiroRepository parceiroRepository;

    /**
     * Cadastra um novo endereço vinculado a um parceiro existente.
     * Evita duplicatas verificando se já existe o mesmo CEP + número
     * para o mesmo parceiro.
     *
     * @param parceiroId ID do parceiro ao qual o endereço será vinculado.
     * @param endereco   Dados do endereço a ser cadastrado (sem ID).
     * @return O endereço persistido com ID gerado.
     * @throws RuntimeException Se o parceiro não existir, ou se o mesmo
     *                          CEP e número já estiverem cadastrados para ele.
     */
    public Endereco cadastrar(Long parceiroId, Endereco endereco) {
        Parceiro parceiro = parceiroRepository.findById(parceiroId)
            .orElseThrow(() -> new RuntimeException(
                "Parceiro não encontrado (ID: " + parceiroId + "). Não é possível vincular o endereço."
            ));

        if (enderecoRepository.existsByCepAndNumeroAndParceiroId(
                endereco.getCep(), endereco.getNumero(), parceiroId)) {
            throw new RuntimeException(
                "Este parceiro já possui um endereço cadastrado com o CEP "
                + endereco.getCep() + " e número " + endereco.getNumero() + "."
            );
        }

        // Padroniza estado em maiúsculas para consistência no banco (ex: "rn" → "RN")
        if (endereco.getEstado() != null) {
            endereco.setEstado(endereco.getEstado().toUpperCase());
        }

        endereco.setParceiro(parceiro);
        return enderecoRepository.save(endereco);
    }

    /**
     * Lista todos os endereços de um parceiro específico.
     * Demonstra reutilização: qualquer consumidor obtém os endereços de
     * qualquer parceiro apenas pelo ID, sem carregar o objeto Parceiro inteiro.
     *
     * @param parceiroId ID do parceiro cujos endereços serão listados.
     * @return Lista de endereços vinculados ao parceiro. Pode ser vazia.
     * @throws RuntimeException Se o parceiro não existir.
     */
    public List<Endereco> listarPorParceiro(Long parceiroId) {
        if (!parceiroRepository.existsById(parceiroId)) {
            throw new RuntimeException("Parceiro não encontrado (ID: " + parceiroId + ").");
        }
        return enderecoRepository.findByParceiroId(parceiroId);
    }

    /**
     * Atualiza os dados de um endereço existente.
     *
     * @param endereco O endereço com os dados atualizados (deve conter ID válido).
     * @throws RuntimeException Se o endereço não for encontrado.
     */
    public void atualizar(Endereco endereco) {
        Endereco existente = enderecoRepository.findById(endereco.getId())
            .orElseThrow(() -> new RuntimeException(
                "Endereço não encontrado para atualização (ID: " + endereco.getId() + ")."
            ));

        // Mantém o vínculo com o parceiro original — endereço não muda de dono
        endereco.setParceiro(existente.getParceiro());

        if (endereco.getEstado() != null) {
            endereco.setEstado(endereco.getEstado().toUpperCase());
        }

        enderecoRepository.save(endereco);
    }

    /**
     * Remove um endereço pelo seu ID.
     * Não afeta nenhum dado do Parceiro ao qual estava vinculado.
     *
     * @param enderecoId ID do endereço a ser removido.
     * @throws RuntimeException Se o endereço não existir.
     */
    public void remover(Long enderecoId) {
        if (!enderecoRepository.existsById(enderecoId)) {
            throw new RuntimeException("Operação cancelada: endereço com ID " + enderecoId + " não existe.");
        }
        enderecoRepository.deleteById(enderecoId);
    }
}