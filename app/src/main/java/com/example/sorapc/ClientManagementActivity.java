package com.example.sorapc;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс ClientManagementActivity представляет собой активность для управления пользователями (клиентами) в приложении.
 *
 * Основное назначение:
 * - Отображение списка пользователей с их текущей информацией, включая имя, email и роль.
 * - Возможность изменения роли пользователя (например, "Client" или "Administrator").
 *
 * Основные функции:
 * - Загрузка и отображение списка пользователей из Firebase Firestore.
 * - Реализация изменения роли пользователя через диалоговое окно.
 * - Интеграция с адаптером ClientAdapter для отображения пользователей в RecyclerView.
 *
 * Поля:
 * - backIcon: Иконка для возврата на предыдущий экран.
 * - clientsRecyclerView: RecyclerView для отображения списка пользователей.
 * - clientAdapter: Адаптер для управления отображением пользователей.
 * - clientList: Список пользователей, загруженных из базы данных.
 * - db: Экземпляр FirebaseFirestore для работы с базой данных.
 *
 * Особенности:
 * - Использует кастомное диалоговое окно для изменения роли пользователя.
 * - Поддерживает обратную связь с пользователем через Toast при выполнении операций.
 * - Интеграция с Header для обеспечения единообразного дизайна навигации.
 */

public class ClientManagementActivity extends AppCompatActivity implements ClientAdapter.OnEditRoleListener {

    private ImageView backIcon;
    private RecyclerView clientsRecyclerView;
    private ClientAdapter clientAdapter;
    private List<User> clientList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_management);

        db = FirebaseFirestore.getInstance();

        backIcon = findViewById(R.id.back_icon);
        clientsRecyclerView = findViewById(R.id.clients_recycler_view);

        clientList = new ArrayList<>();
        clientAdapter = new ClientAdapter(this, clientList, this);
        clientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        clientsRecyclerView.setAdapter(clientAdapter);

        backIcon.setOnClickListener(v -> onBackPressed());

        loadClients();

        View headerView = findViewById(R.id.header);
        new Header(headerView, this);
    }

    private void loadClients() {
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    clientList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        user.setId(document.getId());
                        clientList.add(user);
                    }
                    clientAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки клиентов: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onEditRole(User user) {
        showEditRoleDialog(user);
    }

    private void showEditRoleDialog(User user) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_edit_role, null);

        Spinner roleSpinner = dialogView.findViewById(R.id.role_spinner);
        Button confirmButton = dialogView.findViewById(R.id.button_confirm);
        Button cancelButton = dialogView.findViewById(R.id.button_cancel);

        String[] roles = {"Client", "Administrator"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        int position = roleAdapter.getPosition(user.getRole());
        roleSpinner.setSelection(position);

        View customTitleView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_title, null);
        TextView titleTextView = customTitleView.findViewById(R.id.dialog_title);
        titleTextView.setText("Изменение роли");

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        builder.setCustomTitle(customTitleView)
                .setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        confirmButton.setOnClickListener(v -> {
            String newRole = roleSpinner.getSelectedItem().toString();
            db.collection("users").document(user.getId())
                    .update("role", newRole)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Роль обновлена", Toast.LENGTH_SHORT).show();
                        loadClients();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка обновления роли: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
    }
}