package br.com.terraverde.soa.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

    /**
     * Mapa de tokens ativos: username → token.
     * ConcurrentHashMap garante segurança em ambientes com múltiplas threads.
     */
    private final Map<String, String> tokensPorUsuario = new ConcurrentHashMap<>();

    /**
     * Gera um novo token para o usuário informado, substituindo o anterior se existir.
     *
     * @param username O nome do usuário que está fazendo login.
     * @return O token gerado (UUID aleatório).
     */
    public String gerarToken(String username) {
        String token = UUID.randomUUID().toString();
        tokensPorUsuario.put(username, token);
        return token;
    }

    /**
     * Valida se um token recebido é legítimo para qualquer usuário ativo.
     *
     * @param token O token recebido no cabeçalho SOAP.
     * @return true se o token existir e for válido; false caso contrário.
     */
    public boolean validarToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        // Verifica se o token enviado corresponde a algum usuário ativo
        return tokensPorUsuario.containsValue(token);
    }

    /**
     * Invalida o token do usuário, efetivando o logout.
     *
     * @param username O nome do usuário que está fazendo logout.
     */
    public void invalidarToken(String username) {
        tokensPorUsuario.remove(username);
    }
}