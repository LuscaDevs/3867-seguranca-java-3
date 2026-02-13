package br.com.forum_hub.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.forum_hub.domain.usuario.DadosAtualizacaoUsuario;
import br.com.forum_hub.domain.usuario.DadosCadastroUsuario;
import br.com.forum_hub.domain.usuario.DadosListagemUsuario;
import br.com.forum_hub.domain.usuario.DadosMudancaSenha;
import br.com.forum_hub.domain.usuario.Usuario;
import br.com.forum_hub.domain.usuario.UsuarioService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping("/registrar")
    public ResponseEntity<DadosListagemUsuario> registrar(@RequestBody DadosCadastroUsuario dados,
            UriComponentsBuilder uriBuilder) {

        var usuario = service.cadastrar(dados);
        var uri = uriBuilder.path("/{nomeUsuario}").buildAndExpand(usuario.getNomeUsuario()).toUri();
        return ResponseEntity.created(uri).body(new DadosListagemUsuario(usuario));
    }

    @GetMapping("/{nomeUsuario}")
    public ResponseEntity<DadosListagemUsuario> getUsuarioEntity(@PathVariable String nomeUsuario) {
        var usuario = service.buscarPorNomeUsuario(nomeUsuario);
        return ResponseEntity.ok(new DadosListagemUsuario(usuario));
    }

    @PutMapping("/editar-perfil")
    public ResponseEntity<DadosListagemUsuario> atualizarPerfil(
            @RequestBody DadosAtualizacaoUsuario dados,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {

        var usuarioAtualizado = service.atualizar(usuarioAutenticado, dados);
        return ResponseEntity.ok(new DadosListagemUsuario(usuarioAtualizado));
    }

    @PatchMapping("/alterar-senha")
    public ResponseEntity<String> alterarSenha(
            @RequestBody @Valid DadosMudancaSenha dados,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {

        service.mudarSenha(usuarioAutenticado, dados);
        return ResponseEntity.ok("Senha alterada com sucesso!");
    }

    @DeleteMapping("/deletar-conta")
    public ResponseEntity<String> deletarConta(@AuthenticationPrincipal Usuario usuarioAutenticado) {
        service.deletar(usuarioAutenticado);
        return ResponseEntity.ok("Conta deletada com sucesso!");
    }

    @GetMapping("/verificar-conta")
    public ResponseEntity<String> verificarEmailEntity(@RequestParam String codigo) {
        service.verificarEmail(codigo);
        return ResponseEntity.ok("Conta verificada com sucesso!");
    }

}
