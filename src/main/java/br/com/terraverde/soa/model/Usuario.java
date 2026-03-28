package br.com.terraverde.soa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade JPA para representar um Usuário do sistema.
 * A tabela no banco de dados é tb_usuario.
 */
@Entity
@Table(name = "tb_usuario")
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Nome de usuário
    @NotBlank(message = "O nome de usuário é obrigatório.")
    @Size(min = 4, max = 12, message = "O nome de usuário deve ter entre 4 e 12 caracteres.")
    @Column(unique = true)
    private String username;
    
    // A senha será armazenada criptografada, por isso precisa de um tamanho máximo maior
    @NotBlank(message = "A senha é obrigatória.")
    @Size(min = 6, max = 60)
    private String password; 
}