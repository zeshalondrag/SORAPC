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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText surnameEt, nameEt, middlenameEt, emailEt, phoneEt, passwordEt;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

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

        registerBtn.setOnClickListener(v -> registerUser());

        goToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void registerUser() {
        String surname = surnameEt.getText().toString().trim();
        String name = nameEt.getText().toString().trim();
        String middlename = middlenameEt.getText().toString().trim();
        String email = emailEt.getText().toString().trim();
        String phone = phoneEt.getText().toString().trim();
        String password = passwordEt.getText().toString().trim();

        if (TextUtils.isEmpty(surname)) {
            surnameEt.setError("Введите фамилию");
            return;
        }
        if (TextUtils.isEmpty(name)) {
            nameEt.setError("Введите имя");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            emailEt.setError("Введите email");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            phoneEt.setError("Введите номер телефона");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEt.setError("Введите пароль");
            return;
        }
        if (password.length() < 6) {
            passwordEt.setError("Пароль должен быть не менее 6 символов");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("surname", surname);
                            userData.put("name", name);
                            userData.put("middlename", middlename);
                            userData.put("email", email);
                            userData.put("phone", phone);
                            userData.put("role", "Client");

                            db.collection("users")
                                    .document(user.getUid())
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegisterActivity.this,
                                                "Регистрация успешна",
                                                Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(RegisterActivity.this,
                                                "Ошибка при сохранении данных",
                                                Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Ошибка регистрации: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}