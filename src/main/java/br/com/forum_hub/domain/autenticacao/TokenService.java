package br.com.forum_hub.domain.autenticacao;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;

import br.com.forum_hub.domain.usuario.Usuario;
import br.com.forum_hub.infra.exception.RegraDeNegocioException;

@Service
public class TokenService {
    public String gerarToken(Usuario usuario) {
        try {
            Algorithm algorithm = Algorithm.HMAC256("12345678");
            return JWT.create()
                    .withIssuer("forum-hub")
                    .withSubject(usuario.getUsername())
                    .withExpiresAt(expiracao(30))
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            throw new RegraDeNegocioException("Erro ao gerar token JWT de acesso!");
        }
    }

    public String gerarRefreshToken(Usuario usuario) {
        try {
            Algorithm algorithm = Algorithm.HMAC256("12345678");
            return JWT.create()
                    .withIssuer("forum-hub")
                    .withSubject(usuario.getId().toString())
                    .withExpiresAt(expiracao(120)) // Expira em 2 horas
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            throw new RegraDeNegocioException("Erro ao gerar token JWT de refresh!");
        }
    }

    public void validarToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256("12345678");
            JWT.require(algorithm)
                    .withIssuer("forum-hub")
                    .build()
                    .verify(token);
        } catch (JWTVerificationException e) {
            throw new RegraDeNegocioException("Token JWT de acesso inválido!");
        }
    }

    public String extrairSubject(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256("12345678");
            return JWT.require(algorithm)
                    .withIssuer("forum-hub")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException e) {
            throw new RegraDeNegocioException("Token JWT de acesso inválido!");
        }
    }

    public Instant expiracao(Integer minutos) {
        return LocalDateTime.now().plusMinutes(minutos).toInstant(ZoneOffset.of("-03:00"));
    }
}