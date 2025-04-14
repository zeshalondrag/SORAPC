package com.example.sorapc;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Класс LoginActivity представляет собой активность для авторизации пользователей в приложении.
 *
 * Основное назначение:
 * - Предоставление интерфейса для ввода email и пароля для авторизации.
 * - Обеспечение навигации на экраны регистрации и восстановления пароля.
 * - Проверка учетных данных пользователя и предоставление доступа к приложению при успешной авторизации.
 *
 * Основные функции:
 * - Авторизация пользователя через Firebase Authentication.
 * - Проверка правильности ввода данных (email и пароль).
 * - Получение роли пользователя из Firebase Firestore и передача её в MainActivity.
 * - Переход на экран регистрации (RegisterActivity) и восстановления пароля (ResetPasswordActivity).
 *
 * Поля:
 * - emailEt: Поле ввода email.
 * - passwordEt: Поле ввода пароля.
 * - mAuth: Экземпляр FirebaseAuth для выполнения операций авторизации.
 * - db: Экземпляр FirebaseFirestore для работы с базой данных.
 *
 * Особенности:
 * - Используется Firebase Authentication для проверки учетных данных.
 * - Отображает сообщения об ошибках ввода и авторизации через Toast и Error-сообщения в полях ввода.
 * - Предоставляет удобный интерфейс для навигации между экранами авторизации, регистрации и восстановления пароля.
 */

public class LoginActivity extends AppCompatActivity {
    private EditText emailEt, passwordEt;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEt = findViewById(R.id.email_et);
        passwordEt = findViewById(R.id.password_et);
        Button authBtn = findViewById(R.id.auth_btn);
        TextView goToRegister = findViewById(R.id.go_to_register_activity);
        TextView goToResetPassword = findViewById(R.id.go_to_reset_password_activity);

        authBtn.setOnClickListener(v -> loginUser());

        goToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        goToResetPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = emailEt.getText().toString().trim();
        String password = passwordEt.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEt.setError("Введите email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEt.setError("Введите пароль");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        db.collection("users")
                                .document(mAuth.getCurrentUser().getUid())
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    String role = documentSnapshot.getString("role");
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.putExtra("role", role);
                                    startActivity(intent);
                                    finish();
                                });
                    } else {
                        Toast.makeText(LoginActivity.this, "Неверная почта или пароль!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}