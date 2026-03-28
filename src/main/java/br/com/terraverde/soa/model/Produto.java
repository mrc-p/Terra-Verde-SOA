package br.com.terraverde.soa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidade JPA que representa um Produto do catálogo da Terra Verde.
 *
 * Independente da entidade Parceiro — um produto existe por si só no sistema.
 * A tabela no banco de dados é tb_produto.
 */
@Entity
@Table(name = "tb_produto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do produto é obrigatório.")
    @Size(min = 3, max = 60, message = "O nome deve ter entre 3 e 60 caracteres.")
    private String nome;

    @Size(max = 120, message = "A descrição não deve exceder 120 caracteres.")
    private String descricao;

    @NotNull(message = "O preço é obrigatório.")
    @DecimalMin(value = "0.0", inclusive = false, message = "O preço deve ser maior que zero.")
    @Column(precision = 10, scale = 2)
    private BigDecimal preco;

    @NotBlank(message = "A unidade de medida é obrigatória.")
    @Size(max = 10, message = "A unidade de medida não deve exceder 10 caracteres (ex: KG, UN, L).")
    private String unidadeMedida;

    @CreationTimestamp
    private LocalDateTime dataHoraCadastro;
}