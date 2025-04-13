package com.example.sorapc;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ProductManagementActivity extends AppCompatActivity implements AdminProductAdapter.OnEditProductListener, AdminProductAdapter.OnDeleteProductListener {
    private ImageView backIcon;
    private EditText searchEditText;
    private Button addProductButton;
    private RecyclerView productsRecyclerView;
    private AdminProductAdapter productAdapter;
    private List<Product> productList;
    private FirebaseFirestore db;
    private List<String> categoryTitles;
    private Map<String, String> categoryMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_management);

        db = FirebaseFirestore.getInstance();

        backIcon = findViewById(R.id.back_icon);
        searchEditText = findViewById(R.id.search_edit_text);
        addProductButton = findViewById(R.id.add_product_button);
        productsRecyclerView = findViewById(R.id.products_recycler_view);

        productList = new ArrayList<>();
        categoryTitles = new ArrayList<>();
        categoryMap = new HashMap<>();
        productAdapter = new AdminProductAdapter(this, productList, this, this);
        productsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productsRecyclerView.setAdapter(productAdapter);

        backIcon.setOnClickListener(v -> onBackPressed());

        addProductButton.setOnClickListener(v -> showAddProductDialog());

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadCategories();
        loadProducts();
        listenForProductsChanges();

        View headerView = findViewById(R.id.header);
        new Header(headerView, this);
    }

    private void loadCategories() {
        db.collection("category")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    categoryTitles.clear();
                    categoryMap.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String categoryId = document.getId();
                        String categoryTitle = document.getString("title");
                        if (categoryTitle != null) {
                            categoryTitles.add(categoryTitle);
                            categoryMap.put(categoryTitle, categoryId);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки категорий: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateProductInput(String title, String imageUrl, String priceStr, String article,
                                         String categoryId, String quantityStr, String description,
                                         String gpu, String cpu, String motherboard, String cooling,
                                         String ram, String ssd, String power, String caseName,
                                         EditText editTitle, EditText editPrice, EditText editArticle,
                                         EditText editQuantity, EditText editDescription, EditText editGpu,
                                         EditText editCpu, EditText editMotherboard, EditText editCooling,
                                         EditText editRam, EditText editSsd, EditText editPower, EditText editCase) {
        // Проверка на пустые поля (кроме imageUrl)
        if (title.isEmpty()) {
            editTitle.setError("Введите название товара");
            editTitle.requestFocus();
            return false;
        }
        if (article.isEmpty()) {
            editArticle.setError("Введите артикул");
            editArticle.requestFocus();
            return false;
        }
        if (categoryId == null) {
            Toast.makeText(this, "Выберите категорию", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (quantityStr.isEmpty()) {
            editQuantity.setError("Введите количество");
            editQuantity.requestFocus();
            return false;
        }
        if (description.isEmpty()) {
            editDescription.setError("Введите описание");
            editDescription.requestFocus();
            return false;
        }
        if (gpu.isEmpty()) {
            editGpu.setError("Введите данные о GPU");
            editGpu.requestFocus();
            return false;
        }
        if (cpu.isEmpty()) {
            editCpu.setError("Введите данные о CPU");
            editCpu.requestFocus();
            return false;
        }
        if (motherboard.isEmpty()) {
            editMotherboard.setError("Введите данные о материнской плате");
            editMotherboard.requestFocus();
            return false;
        }
        if (cooling.isEmpty()) {
            editCooling.setError("Введите данные о системе охлаждения");
            editCooling.requestFocus();
            return false;
        }
        if (ram.isEmpty()) {
            editRam.setError("Введите данные о RAM");
            editRam.requestFocus();
            return false;
        }
        if (ssd.isEmpty()) {
            editSsd.setError("Введите данные о SSD");
            editSsd.requestFocus();
            return false;
        }
        if (power.isEmpty()) {
            editPower.setError("Введите данные о блоке питания");
            editPower.requestFocus();
            return false;
        }
        if (caseName.isEmpty()) {
            editCase.setError("Введите данные о корпусе");
            editCase.requestFocus();
            return false;
        }

        // Проверка цены
        long price;
        try {
            price = Long.parseLong(priceStr);
            if (price <= 0) {
                editPrice.setError("Цена должна быть больше 0");
                editPrice.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            editPrice.setError("Цена должна быть числом");
            editPrice.requestFocus();
            return false;
        }

        // Проверка количества
        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity < 0) {
                editQuantity.setError("Количество не может быть отрицательным");
                editQuantity.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            editQuantity.setError("Количество должно быть числом");
            editQuantity.requestFocus();
            return false;
        }

        return true;
    }

    private void showAddProductDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_product, null);

        EditText editTitle = dialogView.findViewById(R.id.edit_product_title);
        EditText editImageUrl = dialogView.findViewById(R.id.edit_product_image_url);
        EditText editPrice = dialogView.findViewById(R.id.edit_product_price);
        EditText editArticle = dialogView.findViewById(R.id.edit_product_article);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinner_product_category);
        EditText editQuantity = dialogView.findViewById(R.id.edit_product_quantity);
        EditText editDescription = dialogView.findViewById(R.id.edit_product_description);
        EditText editGpu = dialogView.findViewById(R.id.edit_product_gpu);
        EditText editCpu = dialogView.findViewById(R.id.edit_product_cpu);
        EditText editMotherboard = dialogView.findViewById(R.id.edit_product_motherboard);
        EditText editCooling = dialogView.findViewById(R.id.edit_product_cooling);
        EditText editRam = dialogView.findViewById(R.id.edit_product_ram);
        EditText editSsd = dialogView.findViewById(R.id.edit_product_ssd);
        EditText editPower = dialogView.findViewById(R.id.edit_product_power);
        EditText editCase = dialogView.findViewById(R.id.edit_product_case);
        Button buttonConfirm = dialogView.findViewById(R.id.button_confirm);
        Button buttonCancel = dialogView.findViewById(R.id.button_cancel);

        String generatedArticle = generateRandomArticle();
        editArticle.setText(generatedArticle);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryTitles);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        View customTitleView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_title, null);
        TextView titleTextView = customTitleView.findViewById(R.id.dialog_title);
        titleTextView.setText("Добавление товара");

        buttonConfirm.setText("Подтвердить добавление");

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        builder.setCustomTitle(customTitleView)
                .setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        buttonConfirm.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            String imageUrl = editImageUrl.getText().toString().trim();
            String priceStr = editPrice.getText().toString().trim();
            String article = editArticle.getText().toString().trim();
            String categoryTitle = spinnerCategory.getSelectedItem() != null ? spinnerCategory.getSelectedItem().toString() : "";
            String categoryId = categoryMap.get(categoryTitle);
            String quantityStr = editQuantity.getText().toString().trim();
            String description = editDescription.getText().toString().trim();
            String gpu = editGpu.getText().toString().trim();
            String cpu = editCpu.getText().toString().trim();
            String motherboard = editMotherboard.getText().toString().trim();
            String cooling = editCooling.getText().toString().trim();
            String ram = editRam.getText().toString().trim();
            String ssd = editSsd.getText().toString().trim();
            String power = editPower.getText().toString().trim();
            String caseName = editCase.getText().toString().trim();

            // Валидация
            if (!validateProductInput(title, imageUrl, priceStr, article, categoryId, quantityStr, description,
                    gpu, cpu, motherboard, cooling, ram, ssd, power, caseName,
                    editTitle, editPrice, editArticle, editQuantity, editDescription,
                    editGpu, editCpu, editMotherboard, editCooling, editRam, editSsd, editPower, editCase)) {
                return;
            }

            long price = Long.parseLong(priceStr);
            int quantity = Integer.parseInt(quantityStr);

            Product product = new Product(
                    article, caseName, cooling, cpu, description, gpu, imageUrl, motherboard,
                    power, price, ram, ssd, title, false, categoryId, quantity, 0L
            );

            db.collection("products").document(article)
                    .set(product)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Товар успешно добавлен", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка добавления товара: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void showEditProductDialog(Product product) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_product, null);

        EditText editTitle = dialogView.findViewById(R.id.edit_product_title);
        EditText editImageUrl = dialogView.findViewById(R.id.edit_product_image_url);
        EditText editPrice = dialogView.findViewById(R.id.edit_product_price);
        EditText editArticle = dialogView.findViewById(R.id.edit_product_article);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinner_product_category);
        EditText editQuantity = dialogView.findViewById(R.id.edit_product_quantity);
        EditText editDescription = dialogView.findViewById(R.id.edit_product_description);
        EditText editGpu = dialogView.findViewById(R.id.edit_product_gpu);
        EditText editCpu = dialogView.findViewById(R.id.edit_product_cpu);
        EditText editMotherboard = dialogView.findViewById(R.id.edit_product_motherboard);
        EditText editCooling = dialogView.findViewById(R.id.edit_product_cooling);
        EditText editRam = dialogView.findViewById(R.id.edit_product_ram);
        EditText editSsd = dialogView.findViewById(R.id.edit_product_ssd);
        EditText editPower = dialogView.findViewById(R.id.edit_product_power);
        EditText editCase = dialogView.findViewById(R.id.edit_product_case);
        Button buttonConfirm = dialogView.findViewById(R.id.button_confirm);
        Button buttonCancel = dialogView.findViewById(R.id.button_cancel);

        editTitle.setText(product.getTitle());
        editImageUrl.setText(product.getImg());
        editPrice.setText(String.valueOf(product.getPrice()));
        editArticle.setText(product.getArticle());
        editArticle.setEnabled(false);
        editQuantity.setText(String.valueOf(product.getQuantity()));
        editDescription.setText(product.getDescription());
        editGpu.setText(product.getGpu());
        editCpu.setText(product.getCpu());
        editMotherboard.setText(product.getMotherboard());
        editCooling.setText(product.getCooling());
        editRam.setText(product.getRam());
        editSsd.setText(product.getSsd());
        editPower.setText(product.getPower());
        editCase.setText(product.getCaseName());

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryTitles);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        String currentCategoryTitle = null;
        for (Map.Entry<String, String> entry : categoryMap.entrySet()) {
            if (entry.getValue().equals(product.getCategory())) {
                currentCategoryTitle = entry.getKey();
                break;
            }
        }
        if (currentCategoryTitle != null) {
            int position = categoryAdapter.getPosition(currentCategoryTitle);
            spinnerCategory.setSelection(position);
        }

        View customTitleView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_title, null);
        TextView titleTextView = customTitleView.findViewById(R.id.dialog_title);
        titleTextView.setText("Изменение товара");

        buttonConfirm.setText("Подтвердить изменения");

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        builder.setCustomTitle(customTitleView)
                .setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        buttonConfirm.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            String imageUrl = editImageUrl.getText().toString().trim();
            String priceStr = editPrice.getText().toString().trim();
            String article = editArticle.getText().toString().trim();
            String categoryTitle = spinnerCategory.getSelectedItem() != null ? spinnerCategory.getSelectedItem().toString() : "";
            String categoryId = categoryMap.get(categoryTitle);
            String quantityStr = editQuantity.getText().toString().trim();
            String description = editDescription.getText().toString().trim();
            String gpu = editGpu.getText().toString().trim();
            String cpu = editCpu.getText().toString().trim();
            String motherboard = editMotherboard.getText().toString().trim();
            String cooling = editCooling.getText().toString().trim();
            String ram = editRam.getText().toString().trim();
            String ssd = editSsd.getText().toString().trim();
            String power = editPower.getText().toString().trim();
            String caseName = editCase.getText().toString().trim();

            // Валидация
            if (!validateProductInput(title, imageUrl, priceStr, article, categoryId, quantityStr, description,
                    gpu, cpu, motherboard, cooling, ram, ssd, power, caseName,
                    editTitle, editPrice, editArticle, editQuantity, editDescription,
                    editGpu, editCpu, editMotherboard, editCooling, editRam, editSsd, editPower, editCase)) {
                return;
            }

            long price = Long.parseLong(priceStr);
            int quantity = Integer.parseInt(quantityStr);

            Product updatedProduct = new Product(
                    article, caseName, cooling, cpu, description, gpu, imageUrl, motherboard,
                    power, price, ram, ssd, title, product.isFavorite(), categoryId, quantity, product.getSalesCount()
            );

            db.collection("products").document(article)
                    .set(updatedProduct)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Товар успешно обновлён", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка обновления товара: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void showDeleteConfirmationDialog(Product product) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_confirm_delete, null);

        TextView messageTextView = dialogView.findViewById(R.id.confirm_delete_message);
        Button buttonDeleteConfirm = dialogView.findViewById(R.id.button_delete_confirm);
        Button buttonDeleteCancel = dialogView.findViewById(R.id.button_delete_cancel);

        // Устанавливаем текст с названием товара
        messageTextView.setText("Вы уверены, что хотите удалить товар \"" + product.getTitle() + "\"?");

        // Настройка заголовка диалога
        View customTitleView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_title, null);
        TextView titleTextView = customTitleView.findViewById(R.id.dialog_title);
        titleTextView.setText("Подтверждение удаления");

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        builder.setCustomTitle(customTitleView)
                .setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        buttonDeleteConfirm.setOnClickListener(v -> {
            // Удаляем товар из Firestore
            db.collection("products").document(product.getArticle())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Товар \"" + product.getTitle() + "\" удалён", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка удаления: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        buttonDeleteCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private String generateRandomArticle() {
        Random random = new Random();
        StringBuilder article = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            article.append(random.nextInt(10));
        }
        return article.toString();
    }

    private void loadProducts() {
        db.collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        productList.add(product);
                    }
                    productAdapter.notifyDataSetChanged();
                    filterProducts();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки товаров: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void listenForProductsChanges() {
        db.collection("products")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Ошибка обновления товаров: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    productList.clear();
                    for (QueryDocumentSnapshot document : value) {
                        Product product = document.toObject(Product.class);
                        productList.add(product);
                    }
                    productAdapter.notifyDataSetChanged();
                    filterProducts();
                });
    }

    private void filterProducts() {
        String query = searchEditText.getText().toString().trim();
        productAdapter.filter(query);
    }

    @Override
    public void onEditProduct(Product product) {
        showEditProductDialog(product);
    }

    @Override
    public void onDeleteProduct(Product product) {
        showDeleteConfirmationDialog(product);
    }
}