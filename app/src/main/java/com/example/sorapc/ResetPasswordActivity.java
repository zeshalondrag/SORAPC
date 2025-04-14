package com.example.sorapc;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * <summary>
 * Класс ResetPasswordActivity предоставляет функционал для восстановления пароля пользователя.
 * Реализует проверку существования email в базе данных Firebase Firestore и отправку письма для сброса пароля через Firebase Authentication.
 * </summary>
 */

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText emailEt;
    private Button sendLetterBtn;
    private ImageView backIcon;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        backIcon = findViewById(R.id.back_icon);
        emailEt = findViewById(R.id.email_et2);
        sendLetterBtn = findViewById(R.id.send_email_btn);

        backIcon.setOnClickListener(v -> onBackPressed());

        String email = getIntent().getStringExtra("email");
        if (email != null && !email.equals("Не указано")) {
            emailEt.setText(email);
        }

        sendLetterBtn.setOnClickListener(v -> sendResetEmail());
    }

    private void sendResetEmail() {
        String email = emailEt.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEt.setError("Введите почту");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEt.setError("Введите корректный email");
            return;
        }

        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(ResetPasswordActivity.this, "Пользователя с такой почтой не существует!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    auth.sendPasswordResetEmail(email)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(ResetPasswordActivity.this, "Письмо для сброса пароля отправлено на почту", Toast.LENGTH_LONG).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ResetPasswordActivity.this, "Ошибка отправки письма: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ResetPasswordActivity.this, "Ошибка проверки почты: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}