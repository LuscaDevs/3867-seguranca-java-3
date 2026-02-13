package br.com.forum_hub.domain.usuario;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.forum_hub.infra.email.EmailService;
import br.com.forum_hub.infra.exception.RegraDeNegocioException;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByEmailIgnoreCaseAndVerificadoTrueAndAtivoTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("O usuário não foi encontrado!"));
    }

    @Transactional
    public Usuario cadastrar(DadosCadastroUsuario dados) {
        var senhaCriptografada = passwordEncoder.encode(dados.senha());

        var usuario = new Usuario(dados, senhaCriptografada);

        emailService.enviarEmailVerificacao(usuario);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario atualizar(Usuario usuario, DadosAtualizacaoUsuario dados) {
        usuario.atualizar(dados);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void mudarSenha(Usuario usuario, DadosMudancaSenha dados) {
        // Validar senha atual
        if (!passwordEncoder.matches(dados.senhaAtual(), usuario.getPassword())) {
            throw new RegraDeNegocioException("Senha atual incorreta!");
        }

        // Validar se não é igual à anterior
        if (passwordEncoder.matches(dados.novaSenha(), usuario.getPassword())) {
            throw new RegraDeNegocioException("Nova senha não pode ser igual à anterior!");
        }

        // Criptografar e atualizar
        var novaSenhaCriptografada = passwordEncoder.encode(dados.novaSenha());
        usuario.alterarSenha(novaSenhaCriptografada);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void deletar(Usuario usuario) {
        usuario.desativar();
        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorNomeUsuario(String nomeUsuario) {
        return usuarioRepository.findByNomeUsuarioIgnoreCase(nomeUsuario)
                .orElseThrow(() -> new RegraDeNegocioException("Usuário não encontrado!"));
    }

    @Transactional
    public void verificarEmail(String codigo) {
        var usuario = usuarioRepository.findByToken(codigo)
                .orElseThrow(() -> new RegraDeNegocioException("Código de verificação inválido!"));

        usuario.verificar();
    }

}
