package es.uca.gamebox.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailRealService implements EmailService{
    private final JavaMailSender sender;
    @Value("${spring.mail.verify.host}")
    private String host;
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Async
    public void sendNewAccountEmail(String name, String to, String token) {
        try{
            String ActivationLink =  host + "/account-activation?token=" + token;

            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setFrom(fromEmail);
            helper.setSubject("Activa tu cuenta en GameBox");

            String htmlContent = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head><meta charset='UTF-8'><style>" +
                    "body { background-color: #2a2a2a; color: white; text-align: center; font-family: sans-serif; padding: 40px; }" +
                    "h1 { font-size: 32px; margin-bottom: 30px; }" +
                    "a.button { display: inline-block; padding: 12px 24px; background-color: #1d5ecf; color: white;" +
                    "text-decoration: none; border-radius: 8px; font-weight: bold; margin-top: 20px; }" +
                    "p { margin: 20px 0; }" +
                    "</style></head>" +
                    "<body>" +
                    "<h1>Activación cuenta GameBox</h1>" +
                    "<p>Hola <strong>" + name + "</strong>,</p>" +
                    "<p>Gracias por registrarte en <strong>GameBox</strong> 🕹️. Para activar tu cuenta, haz clic en el botón de abajo:</p>" +
                    "<p><a href='" + ActivationLink + "' class='button'>Activar cuenta</a></p>" +
                    "<p>Si no te has registrado, ignora este correo.</p>" +
                    "<br><p>Saludos,<br>El equipo de GameBox 🎮</p>" +
                    "</body></html>";


            helper.setText(htmlContent, true); // "true" para indicar que es HTML

            sender.send(message);
        }
        catch (Exception e){
            log.error("Error sending welcome email", e);
        }
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String name, String to, String token) {
        try {
            String resetLink = host + "/reset-password?token=" + token;

            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setFrom(fromEmail);
            helper.setSubject("Restablecimiento de contraseña en GameBox");

            String htmlContent = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head><meta charset='UTF-8'><style>" +
                    "body { background-color: #2a2a2a; color: white; text-align: center; font-family: sans-serif; padding: 40px; }" +
                    "h1 { font-size: 32px; margin-bottom: 30px; }" +
                    "a.button { display: inline-block; padding: 12px 24px; background-color: #1d5ecf; color: white;" +
                    "text-decoration: none; border-radius: 8px; font-weight: bold; margin-top: 20px; }" +
                    "p { margin: 20px 0; }" +
                    "</style></head>" +
                    "<body>" +
                    "<h1>Restablecer tu contraseña</h1>" +
                    "<p>Hola <strong>" + name + "</strong>,</p>" +
                    "<p>Recibiste este correo porque solicitaste restablecer tu contraseña.</p>" +
                    "<p>Para crear una nueva contraseña, haz clic en el botón de abajo:</p>" +
                    "<p><a href='" + resetLink + "' class='button'>Restablecer contraseña</a></p>" +
                    "<p>Si no solicitaste este cambio, puedes ignorar este mensaje.</p>" +
                    "<br><p>Saludos,<br>El equipo de GameBox 🎮</p>" +
                    "</body></html>";

            helper.setText(htmlContent, true); // HTML

            sender.send(message);
        } catch (Exception e) {
            log.error("Error sending reset password email", e);
        }
    }


}
