package br.com.terraverde.soa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidade JPA que representa o Endereço de um Parceiro.
 *
 * Independente do cadastro principal: o endereço pode ser atualizado,
 * consultado ou removido sem que os dados do Parceiro sejam alterados.
 * A tabela no banco de dados é tb_endereco.
 */
@Entity
@Table(name = "tb_endereco")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Vínculo com o Parceiro — um Parceiro pode ter um ou mais endereços
     * (ex: sede, filial, endereço de entrega).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parceiro_id", nullable = false)
    private Parceiro parceiro;

    @NotBlank(message = "O logradouro é obrigatório.")
    @Size(max = 80, message = "O logradouro não deve exceder 80 caracteres.")
    private String logradouro;

    @NotBlank(message = "O número é obrigatório.")
    @Size(max = 10)
    private String numero;

    @Size(max = 30, message = "O complemento não deve exceder 30 caracteres.")
    private String complemento;

    @NotBlank(message = "O bairro é obrigatório.")
    @Size(max = 45)
    private String bairro;

    @NotBlank(message = "A cidade é obrigatória.")
    @Size(max = 45)
    private String cidade;

    @NotBlank(message = "O estado (UF) é obrigatório.")
    @Size(min = 2, max = 2, message = "O estado deve ser a sigla UF com 2 caracteres (ex: RN, SP).")
    private String estado;

    @NotBlank(message = "O CEP é obrigatório.")
    @Size(min = 8, max = 9, message = "O CEP deve ter entre 8 e 9 caracteres (ex: 59000-000).")
    private String cep;

    @CreationTimestamp
    private LocalDateTime dataHoraCadastro;
}