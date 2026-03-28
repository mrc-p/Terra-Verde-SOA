package br.com.terraverde.soa.model;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table; 
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data; 
import lombok.NoArgsConstructor; 

/**
 * Entidade JPA para representar Clientes ou Fornecedores (Parceiros).
 * * A tabela no banco de dados é tb_parceiros.
 */
@Entity
@Table(name = "tb_parceiros") 
@Data
@NoArgsConstructor 
@AllArgsConstructor 
public class Parceiro { 

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Nome
    @NotBlank(message = "O Nome é obrigatório.")
    @Size(min = 5, max = 45, message = "O Nome deve ter entre 5 e 45 caracteres.")
    private String nomeOuRazaoSocial;
    
    // Campo de CPF (para Cliente) ou CNPJ (para Fornecedor)
    @NotBlank(message = "O Documento é obrigatório.")
    @Size(min = 11, max = 18, message = "O Documento deve ter entre 11 e 18 caracteres (incluindo pontuação).")
    @Column(unique = true)
    private String documento;
    
    // Tipo: "CLIENTE" ou "FORNECEDOR"
    @NotBlank(message = "O Tipo de parceiro é obrigatório.")
    private String tipo; 
    
    @NotBlank(message = "O Email é obrigatório.")
    @Email(message = "Email inválido.") 
    @Size(min = 10, max = 45, message = "O Email deve ter entre 10 e 45 caracteres.")
    @Column(unique = true)
    private String email;
    
    @NotBlank(message = "O Telefone é obrigatório.")
    @Size(min = 10, max = 15, message = "O Telefone deve ter entre 10 e 15 caracteres.")
    private String telefone;

    @Size(max = 60, message = "As Observações não devem exceder 60 caracteres.")
    private String observacoes;

    @CreationTimestamp 
    private LocalDateTime dataHoraCadastro;
}