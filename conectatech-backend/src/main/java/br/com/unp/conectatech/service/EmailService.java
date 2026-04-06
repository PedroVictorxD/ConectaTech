package br.com.unp.conectatech.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${mail.from}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void enviarNotificacaoInteresse(String empresaEmail, String alunoNome,
            String alunoEmail, String alunoCurso, String vagaTitulo) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(empresaEmail);
            message.setSubject("Novo interesse na vaga: " + vagaTitulo);
            message.setText(String.format(
                    "O aluno %s (%s, curso: %s) demonstrou interesse na vaga %s.",
                    alunoNome, alunoEmail, alunoCurso != null ? alunoCurso : "não informado", vagaTitulo));

            mailSender.send(message);
            log.info("Email enviado para {} sobre interesse na vaga '{}'", empresaEmail, vagaTitulo);
        } catch (Exception e) {
            log.error("Erro ao enviar email para {}: {}", empresaEmail, e.getMessage());
        }
    }
}
