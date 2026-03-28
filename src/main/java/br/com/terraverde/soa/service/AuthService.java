package br.com.terraverde.soa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.terraverde.soa.model.Usuario;
import br.com.terraverde.soa.repository.UsuarioRepository;


@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Serviço de token injetado — AuthService delega a geração do token,
     * sem conhecer como ou onde ele é armazenado.
     */
    @Autowired
    private TokenService tokenService;

    /**
     * Autentica um usuário com username e senha.
     * Se as credenciais forem válidas, um token de sessão é gerado e retornado.
     *
     * @param username Nome do usuário.
     * @param password Senha em texto puro (será comparada com o hash no banco).
     * @return Token de sessão gerado pelo TokenService.
     * @throws RuntimeException Se as credenciais forem inválidas.
     */
    public String login(String username, String password) {
        return usuarioRepository.findByUsername(username)
            .filter(user -> passwordEncoder.matches(password, user.getPassword()))
            .map(user -> tokenService.gerarToken(user.getUsername()))
            .orElseThrow(() -> new RuntimeException("Credenciais inválidas."));
    }

    /**
     * Registra um novo usuário no sistema com a senha criptografada.
     *
     * @param username Nome de usuário desejado.
     * @param password Senha em texto puro (será armazenada com hash BCrypt).
     * @throws RuntimeException Se o username já estiver em uso.
     */
    public void registrarUsuario(String username, String password) {
        if (usuarioRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Utilizador já existe.");
        }
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(password));
        usuarioRepository.save(u);
    }
}