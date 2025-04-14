package com.example.sorapc;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
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

/**
 * Класс RegisterActivity представляет собой активность для регистрации новых пользователей в приложении SORAPC.
 *
 * Основное назначение:
 * - Предоставление интерфейса для заполнения данных пользователя, их проверки и регистрации.
 * - Отправка кода подтверждения на электронную почту пользователя для завершения регистрации.
 *
 * Основные функции:
 * - Валидация данных, введённых пользователем, таких как фамилия, имя, email, телефон и пароль.
 * - Проверка наличия пользователя с указанным email в базе данных Firebase Firestore.
 * - Регистрация пользователя через Firebase Authentication.
 * - Отправка кода подтверждения на email пользователя через SMTP.
 * - Завершение регистрации после ввода правильного кода подтверждения.
 *
 * Поля:
 * - surnameEt, nameEt, middlenameEt, emailEt, phoneEt, passwordEt: Поля ввода для данных пользователя.
 * - mAuth: Экземпляр FirebaseAuth для выполнения операций регистрации.
 * - db: Экземпляр FirebaseFirestore для работы с базой данных.
 * - generatedCode: Код подтверждения, сгенерированный для пользователя.
 * - userData: Карта данных пользователя для сохранения в Firestore.
 * - email: Email пользователя для отправки кода подтверждения.
 * - isPhoneFormatting: Флаг для предотвращения рекурсии в TextWatcher при форматировании номера телефона.
 *
 * Методы:
 * - setupPhoneNumberFormatting: Настраивает автоматическое форматирование номера телефона (например, "+7").
 * - validateAndRegisterUser: Проверяет данные пользователя и инициирует процесс регистрации.
 * - isValidPassword: Проверяет, соответствует ли пароль минимальным требованиям безопасности.
 * - checkEmailExists: Проверяет, существует ли пользователь с указанным email в базе данных Firestore.
 * - registerUser: Регистрирует пользователя через Firebase Authentication и сохраняет данные в Firestore.
 * - sendVerificationCode: Отправляет код подтверждения на email пользователя через SMTP.
 * - showVerificationDialog: Показывает диалоговое окно для ввода кода подтверждения.
 * - showVerificationDialog: Открывает диалог подтверждения регистрации и завершает регистрацию при правильном коде.
 * - updateUserData: Обновляет данные пользователя в Firestore.
 *
 * Особенности:
 * - Интеграция с Firebase Authentication для регистрации пользователей.
 * - Интеграция с Firebase Firestore для сохранения данных пользователя.
 * - Использование SMTP для отправки кода подтверждения на email.
 * - Реализация форматирования номера телефона и валидации данных.
 * - Поддержка диалогов для подтверждения регистрации.
 */

public class RegisterActivity extends AppCompatActivity {
    private EditText surnameEt, nameEt, middlenameEt, emailEt, phoneEt, passwordEt;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String generatedCode;
    private Map<String, Object> userData;
    private String email;
    private boolean isPhoneFormatting = false; // Флаг для предотвращения рекурсии в TextWatcher

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        surnameEt = findViewById(R.id.surname_et);
        nameEt = findViewById(R.id.name_et);
        middlenameEt = findViewById(R.id.middlename_et);
        emailEt = findViewById(R.id.email_et);
        phoneEt = findViewById(R.id.phone_et);
        passwordEt = findViewById(R.id.password_et);
        Button registerBtn = findViewById(R.id.register_btn);
        TextView goToLogin = findViewById(R.id.go_to_auth_activitys);

        // Настройка форматирования номера телефона
        setupPhoneNumberFormatting();

        registerBtn.setOnClickListener(v -> validateAndRegisterUser());

        goToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void setupPhoneNumberFormatting() {
        phoneEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isPhoneFormatting) return; // Предотвращаем рекурсию

                isPhoneFormatting = true;
                String input = s.toString().trim();

                // Удаляем все нецифровые символы, кроме первого символа, если это "+"
                String digits = input.replaceAll("[^0-9+]", "");
                if (digits.isEmpty()) {
                    phoneEt.setText("");
                    isPhoneFormatting = false;
                    return;
                }

                // Если строка начинается с "+", оставляем его
                if (!digits.startsWith("+")) {
                    // Если пользователь ввёл только цифры, добавляем "+7"
                    digits = "+7" + digits;
                } else if (digits.startsWith("+") && !digits.startsWith("+7")) {
                    // Если пользователь ввёл "+", но не "+7", заменяем на "+7"
                    digits = "+7" + digits.substring(1);
                }

                // Ограничиваем длину номера (например, +7 и 10 цифр)
                if (digits.length() > 12) {
                    digits = digits.substring(0, 12);
                }

                phoneEt.setText(digits);
                phoneEt.setSelection(digits.length()); // Устанавливаем курсор в конец
                isPhoneFormatting = false;
            }
        });
    }

    private void validateAndRegisterUser() {
        String surname = surnameEt.getText().toString().trim();
        String name = nameEt.getText().toString().trim();
        String middlename = middlenameEt.getText().toString().trim();
        email = emailEt.getText().toString().trim();
        String phone = phoneEt.getText().toString().trim();
        String password = passwordEt.getText().toString().trim();

        // Проверка на пустые поля
        if (TextUtils.isEmpty(surname)) {
            surnameEt.setError("Введите фамилию");
            surnameEt.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(name)) {
            nameEt.setError("Введите имя");
            nameEt.requestFocus();
            return;
        }

        // Отчество может быть пустым, но если оно заполнено, проверяем его
        if (!TextUtils.isEmpty(middlename) && middlename.length() < 2) {
            middlenameEt.setError("Отчество должно содержать минимум 2 символа");
            middlenameEt.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailEt.setError("Введите email");
            emailEt.requestFocus();
            return;
        }

        // Проверка на наличие символа "@" в email
        if (!email.contains("@")) {
            emailEt.setError("Email должен содержать символ @");
            emailEt.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            phoneEt.setError("Введите номер телефона");
            phoneEt.requestFocus();
            return;
        }

        // Проверка длины номера телефона (должен быть +7 и 10 цифр, всего 12 символов)
        if (phone.length() != 12 || !phone.startsWith("+7")) {
            phoneEt.setError("Номер телефона должен начинаться с +7 и содержать 10 цифр");
            phoneEt.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEt.setError("Введите пароль");
            passwordEt.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordEt.setError("Пароль должен быть не менее 6 символов");
            passwordEt.requestFocus();
            return;
        }

        if (!isValidPassword(password)) {
            passwordEt.setError("Пароль должен содержать минимум 1 заглавную букву и 1 цифру");
            passwordEt.requestFocus();
            return;
        }

        checkEmailExists(email, surname, name, middlename, phone, password);
    }

    private boolean isValidPassword(String password) {
        boolean hasUpperCase = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            }
            if (Character.isDigit(c)) {
                hasDigit = true;
            }
            if (hasUpperCase && hasDigit) {
                return true;
            }
        }
        return false;
    }

    private void checkEmailExists(String email, String surname, String name, String middlename, String phone, String password) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            Toast.makeText(RegisterActivity.this,
                                    "Пользователь с такой почтой уже существует",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            registerUser(email, password, surname, name, middlename, phone);
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Ошибка проверки email: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser(String email, String password, String surname, String name, String middlename, String phone) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            userData = new HashMap<>();
                            userData.put("surname", surname);
                            userData.put("name", name);
                            userData.put("middlename", middlename);
                            userData.put("email", email);
                            userData.put("phone", phone);
                            userData.put("role", "Client");

                            sendVerificationCode(user.getUid());
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Ошибка регистрации: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendVerificationCode(String userId) {
        generatedCode = String.valueOf(new Random().nextInt(900000) + 100000);

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
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
                message.setSubject("Код подтверждения регистрации");

                MimeBodyPart mimeBodyPart = new MimeBodyPart();
                String htmlContent = "<html>" +
                        "<body style='font-family: Arial, sans-serif; color: #333; background-color: #f4f4f4; padding: 20px;'>" +
                        "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);'>" +
                        "<div style='background-color: #1c2526; color: #ffffff; padding: 20px; text-align: center; border-top-left-radius: 10px; border-top-right-radius: 10px;'>" +
                        "<h1 style='margin: 0; font-size: 24px;'>SORAPC</h1>" +
                        "</div>" +
                        "<div style='padding: 20px;'>" +
                        "<h2 style='color: #1c2526; font-size: 20px; margin-bottom: 15px;'>Подтверждение регистрации</h2>" +
                        "<p>Ваш код подтверждения:</p>" +
                        "<p style='background-color: #f9f9f9; padding: 15px; font-size: 24px; font-weight: bold; text-align: center; border-radius: 5px; border-left: 4px solid #1c2526;'>" + generatedCode + "</p>" +
                        "<p>Введите этот код в приложении, чтобы завершить регистрацию.</p>" +
                        "</div>" +
                        "<div style='background-color: #f4f4f4; padding: 15px; text-align: center; border-bottom-left-radius: 10px; border-bottom-right-radius: 10px;'>" +
                        "<p style='color: #666; font-size: 12px; margin: 0;'>Если вы не запрашивали этот код, просто проигнорируйте это письмо.</p>" +
                        "<p style='color: #666; font-size: 12px; margin: 5px 0 0 0;'>© 2025 SORAPC. Все права защищены.</p>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>";
                mimeBodyPart.setContent(htmlContent, "text/html; charset=utf-8");

                MimeMultipart multipart = new MimeMultipart();
                multipart.addBodyPart(mimeBodyPart);

                message.setContent(multipart);

                Transport.send(message);

                runOnUiThread(() -> showVerificationDialog(userId));

            } catch (MessagingException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this,
                        "Ошибка отправки кода: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void showVerificationDialog(String userId) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_verification_code);
        dialog.setCancelable(false);

        EditText codeEt = dialog.findViewById(R.id.verification_code_et);
        Button confirmBtn = dialog.findViewById(R.id.confirm_code_btn);

        confirmBtn.setOnClickListener(v -> {
            String enteredCode = codeEt.getText().toString().trim();
            if (TextUtils.isEmpty(enteredCode)) {
                codeEt.setError("Введите код");
                return;
            }

            if (enteredCode.equals(generatedCode)) {
                db.collection("users")
                        .document(userId)
                        .set(userData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(RegisterActivity.this,
                                    "Регистрация успешна",
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(RegisterActivity.this,
                                    "Ошибка при сохранении данных",
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        });
            } else {
                Toast.makeText(RegisterActivity.this,
                        "Неверный код подтверждения",
                        Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}