package com.example.sorapc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Properties;
import java.util.Random;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SupportActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private EditText messageInput;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Пожалуйста, авторизуйтесь", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        messageInput = findViewById(R.id.support_message_input);
        sendButton = findViewById(R.id.send_support_button);

        sendButton.setOnClickListener(v -> sendSupportMessage());

        View headerView = findViewById(R.id.header);
        new Header(headerView, this);
        new BottomNavigation(this, R.id.bottom_support);
    }

    private void sendSupportMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (messageText.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, введите сообщение", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = auth.getCurrentUser().getEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Ошибка: не удалось получить email пользователя", Toast.LENGTH_SHORT).show();
            return;
        }

        String ticketNumber = generateTicketNumber();

        new Thread(() -> {
            try {
                Properties properties = new Properties();
                properties.put("mail.smtp.auth", "true");
                properties.put("mail.smtp.starttls.enable", "true");
                properties.put("mail.smtp.host", "smtp.gmail.com");
                properties.put("mail.smtp.port", "587");

                final String senderEmail = "sorapc.store@gmail.com";
                final String senderPassword = "plwc xoyo artf zfor";

                Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(senderEmail, senderPassword);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(senderEmail));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("sorapc.store@gmail.com"));
                message.setSubject("Обращение в поддержку №" + ticketNumber);

                MimeBodyPart mimeBodyPart = new MimeBodyPart();
                String htmlContent = "<html>" +
                        "<body style='font-family: Arial, sans-serif; color: #333; background-color: #f4f4f4; padding: 20px;'>" +
                        "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);'>" +
                        "<div style='background-color: #1c2526; color: #ffffff; padding: 20px; text-align: center; border-top-left-radius: 10px; border-top-right-radius: 10px;'>" +
                        "<h1 style='margin: 0; font-size: 24px;'>SORAPC</h1>" +
                        "</div>" +
                        "<div style='padding: 20px;'>" +
                        "<h2 style='color: #1c2526; font-size: 20px; margin-bottom: 15px;'>Обращение в поддержку</h2>" +
                        "<p><strong>Номер обращения:</strong> #" + ticketNumber + "</p>" +
                        "<p><strong>Email пользователя:</strong> " + userEmail + "</p>" +
                        "<p><strong>Сообщение:</strong></p>" +
                        "<p style='background-color: #f9f9f9; padding: 15px; border-left: 4px solid #1c2526;'>" + messageText.replace("\n", "<br>") + "</p>" +
                        "</div>" +
                        "<div style='background-color: #f4f4f4; padding: 15px; text-align: center; border-bottom-left-radius: 10px; border-bottom-right-radius: 10px;'>" +
                        "<p style='color: #666; font-size: 12px; margin: 0;'>Свяжитесь с нами: <a href='mailto:sorapc.support@gmail.com' style='color: #1c2526;'>sorapc.support@gmail.com</a> | +7 (999) 123-45-67</p>" +
                        "<p style='color: #666; font-size: 12px; margin: 5px 0 0 0;'>© 2023 SORAPC. Все права защищены.</p>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>";
                mimeBodyPart.setContent(htmlContent, "text/html; charset=utf-8");

                MimeMultipart multipart = new MimeMultipart();
                multipart.addBodyPart(mimeBodyPart);

                message.setContent(multipart);

                Transport.send(message);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Сообщение отправлено. Номер обращения: #" + ticketNumber, Toast.LENGTH_LONG).show();
                    messageInput.setText("");
                });

            } catch (MessagingException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Ошибка отправки сообщения: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private String generateTicketNumber() {
        Random random = new Random();
        int number = 10000000 + random.nextInt(90000000);
        return String.valueOf(number);
    }
}