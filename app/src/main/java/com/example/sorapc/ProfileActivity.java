package com.example.sorapc;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

public class ProfileActivity extends AppCompatActivity {

    private TextView surnameTextView, nameTextView, middleNameTextView, emailTextView, passwordTextView, phoneTextView;
    private TextView changePasswordTextView, noCardsText;
    private Button editDataButton, favouritesButton, ordersHistoryButton, adminPanelButton, addCardButton, deleteAccountButton, logOutButton;
    private RecyclerView cardsList;
    private ImageView backIcon;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private List<Card> cards = new ArrayList<>();
    private CardAdapter cardAdapter;
    private String verificationCode;
    private static final int MAX_CARDS = 3; // Ограничение на количество карт

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        backIcon = findViewById(R.id.back_icon);
        surnameTextView = findViewById(R.id.SURNAME);
        nameTextView = findViewById(R.id.NAME);
        middleNameTextView = findViewById(R.id.MIDDLENAME);
        emailTextView = findViewById(R.id.EMAIL);
        passwordTextView = findViewById(R.id.PASSWORD);
        phoneTextView = findViewById(R.id.PHONE);
        changePasswordTextView = findViewById(R.id.change_password);
        noCardsText = findViewById(R.id.no_cards_text);
        cardsList = findViewById(R.id.cards_list);

        editDataButton = findViewById(R.id.edit_data_profile);
        favouritesButton = findViewById(R.id.favourites_profile);
        ordersHistoryButton = findViewById(R.id.orders_history_button);
        adminPanelButton = findViewById(R.id.admin_panel_button);
        addCardButton = findViewById(R.id.add_card_button);
        deleteAccountButton = findViewById(R.id.delete_account_button);
        logOutButton = findViewById(R.id.log_out);

        cardsList.setLayoutManager(new LinearLayoutManager(this));
        cardAdapter = new CardAdapter(cards);
        cardsList.setAdapter(cardAdapter);

        // Добавляем ItemTouchHelper для свайпа
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Card card = cardAdapter.getCard(position);
                showConfirmDeleteCardDialog(position, card);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    View backgroundView = LayoutInflater.from(ProfileActivity.this)
                            .inflate(R.layout.swipe_delete_background, recyclerView, false);

                    backgroundView.setLayoutParams(new RecyclerView.LayoutParams(itemView.getWidth(), itemView.getHeight()));
                    backgroundView.measure(
                            View.MeasureSpec.makeMeasureSpec(itemView.getWidth(), View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(itemView.getHeight(), View.MeasureSpec.EXACTLY)
                    );
                    backgroundView.layout(0, 0, itemView.getWidth(), itemView.getHeight());

                    c.save();
                    c.translate(itemView.getLeft(), itemView.getTop());
                    backgroundView.draw(c);
                    c.restore();

                    // Ограничиваем смещение элемента
                    float swipeThreshold = 0.7f * itemView.getWidth();
                    float newDx = dX;
                    if (Math.abs(newDx) > swipeThreshold) {
                        newDx = Math.signum(newDx) * swipeThreshold;
                    }
                    super.onChildDraw(c, recyclerView, viewHolder, newDx, dY, actionState, isCurrentlyActive);
                } else {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }
        });
        itemTouchHelper.attachToRecyclerView(cardsList);

        View headerView = findViewById(R.id.header);
        new Header(headerView, this);

        backIcon.setOnClickListener(v -> onBackPressed());

        loadUserData();
        loadCards();

        editDataButton.setOnClickListener(v -> showEditDialog());
        favouritesButton.setOnClickListener(v -> openFavourites());
        ordersHistoryButton.setOnClickListener(v -> openOrdersHistory());
        adminPanelButton.setOnClickListener(v -> openAdminPanel());
        addCardButton.setOnClickListener(v -> showAddCardDialog());
        deleteAccountButton.setOnClickListener(v -> showDeleteAccountDialog());
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
                                String role = document.getString("role");

                                surnameTextView.setText(surname != null ? surname : "Не указано");
                                nameTextView.setText(name != null ? name : "Не указано");
                                middleNameTextView.setText(middleName != null ? middleName : "Не указано");
                                emailTextView.setText(email != null ? email : "Не указано");
                                passwordTextView.setText("********");
                                phoneTextView.setText(phone != null ? phone : "Не указано");

                                if ("Administrator".equals(role)) {
                                    adminPanelButton.setVisibility(View.VISIBLE);
                                } else {
                                    adminPanelButton.setVisibility(View.GONE);
                                }
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

    private void loadCards() {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("cards")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Ошибка загрузки карт: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    cards.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot document : value) {
                            Card card = document.toObject(Card.class);
                            card.setCardId(document.getId());
                            cards.add(card);
                        }
                    }

                    updateCardsVisibility();
                    cardAdapter.notifyDataSetChanged();
                });
    }

    private void updateCardsVisibility() {
        if (cards.isEmpty()) {
            noCardsText.setVisibility(View.VISIBLE);
            cardsList.setVisibility(View.GONE);
        } else {
            noCardsText.setVisibility(View.GONE);
            cardsList.setVisibility(View.VISIBLE);
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

    private void showAddCardDialog() {
        // Проверяем количество карт
        if (cards.size() >= MAX_CARDS) {
            Toast.makeText(this, "Вы не можете добавить больше " + MAX_CARDS + " карт", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_card);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText cardNumberInput = dialog.findViewById(R.id.card_number_input);
        EditText cardExpiryInput = dialog.findViewById(R.id.card_expiry_input);
        EditText cardCvvInput = dialog.findViewById(R.id.card_cvv_input);
        Button cancelButton = dialog.findViewById(R.id.cancel_button);
        Button saveButton = dialog.findViewById(R.id.save_button);

        // Форматирование номера карты (пробелы после каждых 4 цифр)
        cardNumberInput.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String input = s.toString().replaceAll("[^0-9]", "");
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < input.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" ");
                    }
                    formatted.append(input.charAt(i));
                }

                cardNumberInput.removeTextChangedListener(this);
                cardNumberInput.setText(formatted.toString());
                cardNumberInput.setSelection(formatted.length());
                cardNumberInput.addTextChangedListener(this);

                isFormatting = false;
            }
        });

        // Форматирование срока действия (MM/YY)
        cardExpiryInput.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String input = s.toString().replaceAll("[^0-9]", "");
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < input.length(); i++) {
                    if (i == 2 && input.length() > 2) {
                        formatted.append("/");
                    }
                    formatted.append(input.charAt(i));
                }

                cardExpiryInput.removeTextChangedListener(this);
                cardExpiryInput.setText(formatted.toString());
                cardExpiryInput.setSelection(formatted.length());
                cardExpiryInput.addTextChangedListener(this);

                isFormatting = false;
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            String cardNumber = cardNumberInput.getText().toString().trim();
            String expiryDate = cardExpiryInput.getText().toString().trim();
            String cvv = cardCvvInput.getText().toString().trim();

            // Валидация
            if (cardNumber.replaceAll(" ", "").length() != 16) {
                Toast.makeText(this, "Номер карты должен содержать 16 цифр", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!expiryDate.matches("^(0[1-9]|1[0-2])/\\d{2}$")) {
                Toast.makeText(this, "Неверный формат срока действия (MM/YY)", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!cvv.matches("^\\d{3}$")) {
                Toast.makeText(this, "CVV/CVC должен содержать 3 цифры", Toast.LENGTH_SHORT).show();
                return;
            }

            Card card = new Card(cardNumber, expiryDate, cvv);
            String userId = auth.getCurrentUser().getUid();
            db.collection("users").document(userId)
                    .collection("cards")
                    .add(card)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Карта добавлена", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка добавления карты: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        dialog.show();
    }

    private void showConfirmDeleteCardDialog(int position, Card card) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_confirm_delete_card);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button cancelButton = dialog.findViewById(R.id.cancel_button);
        Button deleteButton = dialog.findViewById(R.id.delete_button);

        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
            cardAdapter.notifyItemChanged(position); // Возвращаем элемент на место
        });

        deleteButton.setOnClickListener(v -> {
            String userId = auth.getCurrentUser().getUid();
            db.collection("users").document(userId)
                    .collection("cards")
                    .document(card.getCardId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ProfileActivity.this, "Карта удалена", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "Ошибка удаления карты: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        cardAdapter.notifyItemChanged(position); // Возвращаем элемент, если удаление не удалось
                    });
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDeleteAccountDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_delete_account);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button cancelButton = dialog.findViewById(R.id.cancel_button);
        Button deleteButton = dialog.findViewById(R.id.delete_button);

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        deleteButton.setOnClickListener(v -> {
            dialog.dismiss();
            sendVerificationCode();
        });

        dialog.show();
    }

    private void sendVerificationCode() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = user.getEmail();
        verificationCode = String.format("%06d", new Random().nextInt(999999));

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
                message.setSubject("Код подтверждения для удаления аккаунта");

                MimeBodyPart mimeBodyPart = new MimeBodyPart();
                String htmlContent = "<html>" +
                        "<body style='font-family: Arial, sans-serif; color: #333; background-color: #f4f4f4; padding: 20px;'>" +
                        "<div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);'>" +
                        "<div style='background-color: #1c2526; color: #ffffff; padding: 20px; text-align: center; border-top-left-radius: 10px; border-top-right-radius: 10px;'>" +
                        "<h1 style='margin: 0; font-size: 24px;'>SORAPC</h1>" +
                        "</div>" +
                        "<div style='padding: 20px;'>" +
                        "<h2 style='color: #1c2526; font-size: 20px; margin-bottom: 15px;'>Подтверждение удаления аккаунта</h2>" +
                        "<p>Ваш код подтверждения:</p>" +
                        "<p style='background-color: #f9f9f9; padding: 15px; font-size: 24px; font-weight: bold; text-align: center; border-radius: 5px; border-left: 4px solid #1c2526;'>" + verificationCode + "</p>" +
                        "<p>Введите этот код в приложении, чтобы подтвердить удаление аккаунта.</p>" +
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

                runOnUiThread(this::showVerificationDialog);

            } catch (MessagingException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Ошибка отправки кода: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void showVerificationDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_verification_code);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText codeInput = dialog.findViewById(R.id.verification_code_et);
        Button confirmButton = dialog.findViewById(R.id.confirm_code_btn);

        confirmButton.setOnClickListener(v -> {
            String enteredCode = codeInput.getText().toString().trim();
            if (enteredCode.equals(verificationCode)) {
                deleteAccount();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Неверный код подтверждения", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void deleteAccount() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        // Удаляем данные пользователя из Firestore
        db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Удаляем пользователя из Authentication
                    user.delete()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(this, "Аккаунт удалён", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(this, "Ошибка удаления аккаунта: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка удаления данных: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void openFavourites() {
        Intent intent = new Intent(ProfileActivity.this, FavouritesActivity.class);
        startActivity(intent);
    }

    private void openOrdersHistory() {
        Intent intent = new Intent(ProfileActivity.this, OrdersHistoryActivity.class);
        startActivity(intent);
    }

    private void openAdminPanel() {
        Intent intent = new Intent(ProfileActivity.this, AdminPanelActivity.class);
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