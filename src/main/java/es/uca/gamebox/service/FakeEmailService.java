package es.uca.gamebox.service;

public class FakeEmailService implements EmailService {
    @Override
    public void sendNewAccountEmail(String to, String subject, String content) {
        System.out.println("Simulando envío de email a " + to);
    }

    @Override
    public void sendPasswordResetEmail(String to, String subject, String content) {
        System.out.println("Simulando envío de email de restablecimiento de contraseña a " + to);
    }


    public void verifyEmail(String email) {
        System.out.println("Simulando verificación de email: " + email);
    }
}