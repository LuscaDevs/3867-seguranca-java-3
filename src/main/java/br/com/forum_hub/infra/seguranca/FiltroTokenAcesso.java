package br.com.forum_hub.infra.seguranca;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import br.com.forum_hub.domain.autenticacao.TokenService;
import br.com.forum_hub.domain.usuario.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@Component
public class FiltroTokenAcesso extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    FiltroTokenAcesso(TokenService tokenService, UsuarioRepository usuarioRepository) {
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Recuperar o token do header Authorization
        String token = request.getHeader("Authorization");

        if (token != null) {
            // Remover o prefixo "Bearer " do token
            token = token.replace("Bearer ", "");

            // Validar e extrair o username do token
            String username = tokenService.extrairSubject(token);

            // Buscar o usuário no banco de dados
            var usuario = usuarioRepository.findByEmailIgnoreCaseAndVerificadoTrue(username);

            // Criar autenticação no Spring Security com o usuário completo
            if (usuario.isPresent()) {
                var autenticacao = new UsernamePasswordAuthenticationToken(usuario.get(), null, null);
                SecurityContextHolder.getContext().setAuthentication(autenticacao);
            }
        }

        filterChain.doFilter(request, response);
    }

}
