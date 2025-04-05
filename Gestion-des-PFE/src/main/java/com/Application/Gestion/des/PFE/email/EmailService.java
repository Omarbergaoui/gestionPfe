package com.Application.Gestion.des.PFE.email;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    public void sendEmail(String to,String activationCode){
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            String activationLink = "http://localhost:4200/ForgetPassword/" + activationCode;
            helper.setTo(to);
            helper.setSubject("Mot de passe oublié");
            helper.setText(
                    "<div style=\"font-family: Arial, sans-serif; text-align: center; padding: 20px; background-color: #f4f4f4;\">"
                            + "<div style=\"max-width: 500px; background: white; padding: 20px; border-radius: 10px; box-shadow: 0px 4px 6px rgba(0,0,0,0.1);\">"
                            + "<h2 style=\"color: #333;\">Réinitialisation de votre mot de passe</h2>"
                            + "<p style=\"color: #555; font-size: 16px;\">Vous avez demandé la réinitialisation de votre mot de passe.</p>"
                            + "<p style=\"color: #555; font-size: 16px;\">Cliquez sur le bouton ci-dessous pour procéder :</p>"
                            + "<a href=\"" + activationLink + "\" style=\"display: inline-block; padding: 10px 20px; margin-top: 10px; background-color: #007bff; color: white; text-decoration: none; font-size: 18px; border-radius: 5px;\">Changer mon mot de passe</a>"
                            + "<p style=\"color: #777; font-size: 14px; margin-top: 15px;\">Ce lien expirera dans <strong>3 heures</strong>.</p>"
                            + "<p style=\"color: #777; font-size: 14px;\">Si vous n'avez pas demandé cette action, ignorez simplement cet email.</p>"
                            + "</div>"
                            + "</div>",
                    true
            );
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'e-mail", e);
        }
    }
    public void sendActivationEmail(String to, String activationCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String activationLink = "http://localhost:8080/Authentication/activate?code=" + activationCode;

            helper.setTo(to);
            helper.setSubject("Activation de votre compte");

            helper.setText(
                    "<div style=\"font-family: Arial, sans-serif; text-align: center; padding: 20px; background-color: #f4f4f4;\">"
                            + "<div style=\"max-width: 500px; background: white; padding: 20px; border-radius: 10px; box-shadow: 0px 4px 6px rgba(0,0,0,0.1);\">"
                            + "<h2 style=\"color: #333;\">Bienvenue !</h2>"
                            + "<p style=\"color: #555; font-size: 16px;\">Merci de vous être inscrit. Veuillez activer votre compte en cliquant sur le bouton ci-dessous :</p>"
                            + "<a href=\"" + activationLink + "\" style=\"display: inline-block; padding: 10px 20px; margin-top: 10px; background-color: #28a745; color: white; text-decoration: none; font-size: 18px; border-radius: 5px;\">Activer mon compte</a>"
                            + "<p style=\"color: #777; font-size: 14px;\">Si vous n'avez pas créé de compte, ignorez simplement cet email.</p>"
                            + "</div>"
                            + "</div>",
                    true
            );

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'e-mail d'activation", e);
        }
    }

}
