package br.com.unp.conectatech.service;

import br.com.unp.conectatech.exception.EmailDeliveryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${mail.from}")
    private String from;

    @Value("${mail.reset-password-url}")
    private String resetPasswordUrl;

    @Value("${mail.confirm-email-url}")
    private String confirmEmailUrl;

    @Value("${auth.reset-password.expiration-hours}")
    private long resetPasswordExpirationHours;

    @Value("${auth.email-confirmation.expiration-hours}")
    private long emailConfirmationExpirationHours;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarConfirmacaoEmail(String destinatario, String nome, String token, String tipoConta) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(destinatario);
            message.setSubject("Confirme seu email - ConectaTech");
            message.setText(String.format(
                    "Olá, %s.%n%nSua conta %s foi criada no ConectaTech.%n%n" +
                            "Para ativá-la, confirme seu email no link abaixo:%n%s%n%n" +
                            "Se preferir, utilize este token manualmente:%n%s%n%n" +
                            "O token expira em %d hora(s). Se você não criou esta conta, ignore este email.",
                    nome,
                    tipoConta,
                    buildConfirmLink(token, tipoConta),
                    token,
                    emailConfirmationExpirationHours));

            mailSender.send(message);
            log.info("Email de confirmacao enviado para {}", destinatario);
        } catch (Exception e) {
            throw new EmailDeliveryException("Não foi possível enviar o email de confirmação", e);
        }
    }

    public void enviarRecuperacaoSenha(String destinatario, String nome, String token, String tipoConta) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(destinatario);
            message.setSubject("Recuperação de senha - ConectaTech");
            message.setText(String.format(
                    "Olá, %s.%n%nRecebemos uma solicitação para redefinir a senha da sua conta %s no ConectaTech.%n%n" +
                            "Acesse o link abaixo para continuar:%n%s%n%n" +
                            "Se preferir, utilize este token manualmente:%n%s%n%n" +
                            "O token expira em %d hora(s). Se você não solicitou esta alteração, ignore este email.",
                    nome,
                    tipoConta,
                    buildResetLink(token, tipoConta),
                    token,
                    resetPasswordExpirationHours));

            mailSender.send(message);
            log.info("Email de recuperação de senha enviado para {}", destinatario);
        } catch (Exception e) {
            throw new EmailDeliveryException("Não foi possível enviar o email de recuperação de senha", e);
        }
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

    private String buildResetLink(String token, String tipoConta) {
        String separador = resetPasswordUrl.contains("?") ? "&" : "?";
        return resetPasswordUrl
                + separador
                + "token=" + URLEncoder.encode(token, StandardCharsets.UTF_8)
                + "&tipo=" + URLEncoder.encode(tipoConta, StandardCharsets.UTF_8);
    }

    private String buildConfirmLink(String token, String tipoConta) {
        String separador = confirmEmailUrl.contains("?") ? "&" : "?";
        return confirmEmailUrl
                + separador
                + "token=" + URLEncoder.encode(token, StandardCharsets.UTF_8)
                + "&tipo=" + URLEncoder.encode(tipoConta, StandardCharsets.UTF_8);
    }
}
