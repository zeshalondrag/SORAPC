package com.example.sorapc;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
 * <summary>
 * Класс CategoryManagementActivity представляет активность для управления категориями.
 * Предоставляет функционал добавления, редактирования и удаления категорий с интеграцией Firebase Firestore.
 * </summary>
 */

public class CategoryManagementActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryActionListener {

    private ImageView backIcon;
    private Button addCategoryButton;
    private RecyclerView categoriesRecyclerView;
    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);

        db = FirebaseFirestore.getInstance();

        backIcon = findViewById(R.id.back_icon);
        addCategoryButton = findViewById(R.id.add_category_button);
        categoriesRecyclerView = findViewById(R.id.categories_recycler_view);

        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(this, categoryList, this);
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        categoriesRecyclerView.setAdapter(categoryAdapter);

        backIcon.setOnClickListener(v -> onBackPressed());
        addCategoryButton.setOnClickListener(v -> showAddCategoryDialog());

        loadCategories();

        View headerView = findViewById(R.id.header);
        new Header(headerView, this);
    }

    private void loadCategories() {
        db.collection("category")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    categoryList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Category category = document.toObject(Category.class);
                        category.setId(document.getId());
                        categoryList.add(category);
                    }
                    categoryAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки категорий: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showAddCategoryDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_category, null);

        EditText titleInput = dialogView.findViewById(R.id.category_title_input);
        Button confirmButton = dialogView.findViewById(R.id.button_confirm);
        Button cancelButton = dialogView.findViewById(R.id.button_cancel);

        View customTitleView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_title, null);
        TextView titleTextView = customTitleView.findViewById(R.id.dialog_title);
        titleTextView.setText("Добавление категории");

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        builder.setCustomTitle(customTitleView)
                .setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        confirmButton.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "Введите название категории", Toast.LENGTH_SHORT).show();
                return;
            }

            Category category = new Category(title);
            db.collection("category")
                    .add(category)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Категория добавлена", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadCategories();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка добавления категории: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
    }

    @Override
    public void onEditCategory(Category category) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_category, null);

        EditText titleInput = dialogView.findViewById(R.id.category_title_input);
        Button confirmButton = dialogView.findViewById(R.id.button_confirm);
        Button cancelButton = dialogView.findViewById(R.id.button_cancel);

        titleInput.setText(category.getTitle());

        View customTitleView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_title, null);
        TextView titleTextView = customTitleView.findViewById(R.id.dialog_title);
        titleTextView.setText("Изменение категории");

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        builder.setCustomTitle(customTitleView)
                .setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        confirmButton.setOnClickListener(v -> {
            String newTitle = titleInput.getText().toString().trim();
            if (newTitle.isEmpty()) {
                Toast.makeText(this, "Введите название категории", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("category").document(category.getId())
                    .update("title", newTitle)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Категория обновлена", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadCategories();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка обновления категории: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
    }

    @Override
    public void onDeleteCategory(Category category) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_confirm_delete_category, null);

        TextView messageTextView = dialogView.findViewById(R.id.confirm_delete_message);
        Button confirmButton = dialogView.findViewById(R.id.button_delete_confirm);
        Button cancelButton = dialogView.findViewById(R.id.button_delete_cancel);

        messageTextView.setText("Вы уверены, что хотите удалить категорию \"" + category.getTitle() + "\"?");

        View customTitleView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_title, null);
        TextView titleTextView = customTitleView.findViewById(R.id.dialog_title);
        titleTextView.setText("Подтверждение удаления");

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        builder.setCustomTitle(customTitleView)
                .setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        confirmButton.setOnClickListener(v -> {
            db.collection("category").document(category.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Категория удалена", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadCategories();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка удаления категории: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
    }
}