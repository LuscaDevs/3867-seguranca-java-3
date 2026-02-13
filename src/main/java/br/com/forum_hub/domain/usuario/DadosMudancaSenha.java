package br.com.forum_hub.domain.usuario;

import jakarta.validation.constraints.Size;

public record DadosMudancaSenha(
        String senhaAtual,
        @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres") String novaSenha) {
}