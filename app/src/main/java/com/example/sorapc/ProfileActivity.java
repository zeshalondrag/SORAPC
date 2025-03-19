package com.example.sorapc;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView surnameTextView, nameTextView, middleNameTextView, emailTextView, passwordTextView, phoneTextView;
    private TextView changePasswordTextView;
    private Button editDataButton, favouritesButton, logOutButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        surnameTextView = findViewById(R.id.SURNAME);
        nameTextView = findViewById(R.id.NAME);
        middleNameTextView = findViewById(R.id.MIDDLENAME);
        emailTextView = findViewById(R.id.EMAIL);
        passwordTextView = findViewById(R.id.PASSWORD);
        phoneTextView = findViewById(R.id.PHONE);
        changePasswordTextView = findViewById(R.id.change_password);

        editDataButton = findViewById(R.id.edit_data_profile);
        favouritesButton = findViewById(R.id.favourites_profile);
        logOutButton = findViewById(R.id.log_out);

        View headerView = findViewById(R.id.header);
        new Header(headerView, this);

        loadUserData();

        editDataButton.setOnClickListener(v -> showEditDialog());
        favouritesButton.setOnClickListener(v -> openFavourites());
        logOutButton.setOnClickListener(v -> logOut());

        changePasswordTextView.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserData() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("users").document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String surname = document.getString("surname");
                                String name = document.getString("name");
                                String middleName = document.getString("middlename");
                                String email = document.getString("email");
                                String password = document.getString("password");
                                String phone = document.getString("phone");

                                surnameTextView.setText(surname != null ? surname : "Не указано");
                                nameTextView.setText(name != null ? name : "Не указано");
                                middleNameTextView.setText(middleName != null ? middleName : "Не указано");
                                emailTextView.setText(email != null ? email : "Не указано");
                                passwordTextView.setText("********");
                                phoneTextView.setText(phone != null ? phone : "Не указано");
                            } else {
                                Toast.makeText(ProfileActivity.this, "Данные пользователя не найдены", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ProfileActivity.this, "Ошибка загрузки данных: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void showEditDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_edit_profile, null);

        EditText editSurname = dialogView.findViewById(R.id.edit_surname);
        EditText editName = dialogView.findViewById(R.id.edit_name);
        EditText editMiddleName = dialogView.findViewById(R.id.edit_middlename);
        EditText editEmail = dialogView.findViewById(R.id.edit_email);
        EditText editPhone = dialogView.findViewById(R.id.edit_phone);
        Button buttonSave = dialogView.findViewById(R.id.button_save);
        Button buttonCancel = dialogView.findViewById(R.id.button_cancel);

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            editSurname.setText(document.getString("surname"));
                            editName.setText(document.getString("name"));
                            editMiddleName.setText(document.getString("middlename"));
                            editEmail.setText(document.getString("email"));
                            editPhone.setText(document.getString("phone"));
                        }
                    });
        }

        View customTitleView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_title, null);
        TextView titleTextView = customTitleView.findViewById(R.id.dialog_title);
        titleTextView.setText("Изменение данных");

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        builder.setCustomTitle(customTitleView)
                .setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        buttonSave.setOnClickListener(v -> {
            String newSurname = editSurname.getText().toString().trim();
            String newName = editName.getText().toString().trim();
            String newMiddleName = editMiddleName.getText().toString().trim();
            String newEmail = editEmail.getText().toString().trim();
            String newPhone = editPhone.getText().toString().trim();

            updateUserData(newSurname, newName, newMiddleName, newEmail, newPhone);
            dialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void updateUserData(String surname, String name, String middlename, String email, String phone) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Map<String, Object> updates = new HashMap<>();
            updates.put("surname", surname);
            updates.put("name", name);
            updates.put("middlename", middlename);
            updates.put("email", email);
            updates.put("phone", phone);

            db.collection("users").document(userId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ProfileActivity.this, "Данные обновлены", Toast.LENGTH_SHORT).show();
                        loadUserData();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "Ошибка обновления данных", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void openFavourites() {
        Intent intent = new Intent(ProfileActivity.this, FavouritesActivity.class);
        startActivity(intent);
    }

    private void logOut() {
        auth.signOut();
        Toast.makeText(ProfileActivity.this, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}