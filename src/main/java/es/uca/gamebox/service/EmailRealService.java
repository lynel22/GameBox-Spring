package es.uca.gamebox.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom(fromEmail);
            message.setSubject("Verificación de cuenta en GameBox");
            message.setText("Hola " + name + ",\n\n" +
                    "Gracias por registrarte en GameBox. Para activar tu cuenta, haz clic en el siguiente enlace:\n" +
                    "http://" + host + "/verify?token=" + token + "\n\n" +
                    "Si no te has registrado en GameBox, ignora este correo.\n\n" +
                    "Saludos,\nEl equipo de GameBox");
        }
        catch (Exception e){
            log.error("Error sending welcome email", e);
        }
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String name, String to, String token) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom(fromEmail);
            message.setSubject("Restablecimiento de contraseña en GameBox");
            message.setText("Hola " + name + ",\n\n" +
                    "Recibiste este correo porque solicitaste restablecer tu contraseña. Para hacerlo, haz clic en el siguiente enlace:\n" +
                    "http://" + host + "/verify/password?token=" + token + "\n\n" +
                    "Si no solicitaste este cambio, ignora este correo.\n\n" +
                    "Saludos,\nEl equipo de GameBox");
        }
        catch (Exception e){
            log.error("Error sending reset password email", e);
        }
    }

}
