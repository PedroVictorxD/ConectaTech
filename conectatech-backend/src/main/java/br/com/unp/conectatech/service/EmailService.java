package br.com.unp.conectatech.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public void enviarConfirmacaoEmail(String destinatario, String nome, String token, String tipoConta) {
        log.info("========================================");
        log.info("SIMULACAO DE EMAIL - CONFIRMACAO");
        log.info("Para: {}", destinatario);
        log.info("Token: {}", token);
        log.info("Tipo: {}", tipoConta);
        log.info("========================================");
    }

    public void enviarRecuperacaoSenha(String destinatario, String nome, String token, String tipoConta) {
        log.info("========================================");
        log.info("SIMULACAO DE EMAIL - RECUPERACAO DE SENHA");
        log.info("Para: {}", destinatario);
        log.info("Token: {}", token);
        log.info("Tipo: {}", tipoConta);
        log.info("========================================");
    }

    public void enviarNotificacaoInteresse(String empresaEmail, String alunoNome,
            String alunoEmail, String alunoCurso, String vagaTitulo) {
        log.info("========================================");
        log.info("SIMULACAO DE EMAIL - INTERESSE NA VAGA");
        log.info("Para: {}", empresaEmail);
        log.info("Aluno: {} ({})", alunoNome, alunoEmail);
        log.info("Vaga: {}", vagaTitulo);
        log.info("========================================");
    }
}
