package com.example.sorapc;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс CatalogActivity представляет собой активность, предназначенную для отображения каталога товаров
 * с возможностью фильтрации, поиска и отображения статуса избранных товаров.
 *
 * Основное назначение:
 * - Отображение товаров в виде сетки с помощью RecyclerView.
 * - Реализация фильтрации товаров по категории, цене и текстовому запросу.
 * - Интеграция с Firebase Firestore для загрузки данных о товарах и категориях.
 * - Синхронизация статуса "Избранное" для товаров на основе данных пользователя.
 *
 * Основные функции:
 * - Загрузка списка товаров и категорий из Firebase Firestore.
 * - Фильтрация товаров по выбранной категории, текстовому запросу или порядку сортировки цены.
 * - Поддержка поиска товаров в режиме реального времени.
 * - Слушание изменений в данных товаров и избранного для обновления интерфейса.
 * - Инициализация выбранной категории из Intent, переданной из другой активности.
 *
 * Поля:
 * - searchEditText: Поле для ввода текста поиска.
 * - priceFilterSpinner: Спиннер для выбора порядка сортировки цены.
 * - categoryFilterSpinner: Спиннер для выбора категории.
 * - productsRecyclerView: RecyclerView для отображения списка товаров.
 * - productAdapter: Адаптер для управления отображением товаров.
 * - productList: Список товаров, загруженных из базы данных.
 * - categoryTitles: Список названий категорий.
 * - categoryMap: Карта соответствия названий категорий их идентификаторам.
 * - selectedCategory: Выбранная категория, переданная из Intent.
 *
 * Особенности:
 * - Поддерживает динамическую загрузку категорий и установку начального фильтра по категории.
 * - Синхронизирует список избранных товаров с базой данных.
 * - Интеграция с Header и BottomNavigation для единообразной навигации в приложении.
 */

public class CatalogActivity extends AppCompatActivity {

    private EditText searchEditText;
    private Spinner priceFilterSpinner, categoryFilterSpinner;
    private RecyclerView productsRecyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private List<String> categoryTitles;
    private Map<String, String> categoryMap;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String selectedCategory; // Для хранения категории из Intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        searchEditText = findViewById(R.id.search_edit_text);
        priceFilterSpinner = findViewById(R.id.price_filter_spinner);
        categoryFilterSpinner = findViewById(R.id.category_filter_spinner);
        productsRecyclerView = findViewById(R.id.products_recycler_view);

        productList = new ArrayList<>();
        categoryTitles = new ArrayList<>();
        categoryMap = new HashMap<>();
        categoryTitles.add("Все");
        categoryMap.put("Все", "Все");
        productAdapter = new ProductAdapter(this, productList);
        productsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productsRecyclerView.setAdapter(productAdapter);

        List<String> priceSortOptions = new ArrayList<>();
        priceSortOptions.add("По убыванию");
        priceSortOptions.add("По возрастанию");
        ArrayAdapter<String> priceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priceSortOptions);
        priceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        priceFilterSpinner.setAdapter(priceAdapter);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryTitles);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryFilterSpinner.setAdapter(categoryAdapter);

        // Получаем категорию из Intent
        selectedCategory = getIntent().getStringExtra("category");

        loadCategories();
        loadProducts();

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

        priceFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterProducts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        categoryFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterProducts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        View headerView = findViewById(R.id.header);
        new Header(headerView, this);
        new BottomNavigation(this, R.id.bottom_catalog);

        listenForFavoritesChanges();
        listenForProductsChanges();
    }

    private void loadCategories() {
        db.collection("category")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    categoryTitles.clear();
                    categoryMap.clear();
                    categoryTitles.add("Все");
                    categoryMap.put("Все", "Все");
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String categoryId = document.getId();
                        String categoryTitle = document.getString("title");
                        if (categoryTitle != null) {
                            categoryTitles.add(categoryTitle);
                            categoryMap.put(categoryTitle, categoryId);
                        }
                    }
                    ((ArrayAdapter) categoryFilterSpinner.getAdapter()).notifyDataSetChanged();

                    // После загрузки категорий устанавливаем выбранную категорию
                    if (selectedCategory != null) {
                        int position = categoryTitles.indexOf(selectedCategory);
                        if (position != -1) {
                            categoryFilterSpinner.setSelection(position);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Обработка ошибки
                });
    }

    private void loadProducts() {
        db.collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        product.setFavorite(false);
                        productList.add(product);
                    }

                    syncFavorites();
                    productAdapter.notifyDataSetChanged();
                    filterProducts();
                })
                .addOnFailureListener(e -> {
                    // Обработка ошибки
                });
    }

    private void listenForProductsChanges() {
        db.collection("products")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    productList.clear();
                    for (QueryDocumentSnapshot document : value) {
                        Product product = document.toObject(Product.class);
                        product.setFavorite(false);
                        productList.add(product);
                    }
                    syncFavorites();
                    productAdapter.notifyDataSetChanged();
                    filterProducts();
                });
    }

    private void syncFavorites() {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("favorites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> favoriteArticles = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        favoriteArticles.add(product.getArticle());
                    }
                    for (Product product : productList) {
                        product.setFavorite(favoriteArticles.contains(product.getArticle()));
                    }
                    productAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Обработка ошибки
                });
    }

    private void listenForFavoritesChanges() {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("favorites")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    List<String> favoriteArticles = new ArrayList<>();
                    for (QueryDocumentSnapshot document : value) {
                        Product product = document.toObject(Product.class);
                        favoriteArticles.add(product.getArticle());
                    }
                    for (Product product : productList) {
                        product.setFavorite(favoriteArticles.contains(product.getArticle()));
                    }
                    productAdapter.notifyDataSetChanged();
                });
    }

    private void filterProducts() {
        String query = searchEditText.getText().toString().trim();
        String priceSort = priceFilterSpinner.getSelectedItem() != null ? priceFilterSpinner.getSelectedItem().toString() : "По убыванию";
        String categoryTitle = categoryFilterSpinner.getSelectedItem() != null ? categoryFilterSpinner.getSelectedItem().toString() : "Все";
        String categoryId = categoryMap.get(categoryTitle);
        productAdapter.filter(query, priceSort, categoryId);
    }
}