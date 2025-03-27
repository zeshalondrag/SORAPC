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
import java.util.List;

public class CatalogActivity extends AppCompatActivity {

    private EditText searchEditText;
    private Spinner priceFilterSpinner, categoryFilterSpinner;
    private RecyclerView productsRecyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private List<String> categories;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

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
        productAdapter = new ProductAdapter(this, productList);
        productsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productsRecyclerView.setAdapter(productAdapter);

        List<String> priceSortOptions = new ArrayList<>();
        priceSortOptions.add("По убыванию");
        priceSortOptions.add("По возрастанию");
        ArrayAdapter<String> priceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priceSortOptions);
        priceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        priceFilterSpinner.setAdapter(priceAdapter);

        categories = new ArrayList<>();
        categories.add("Все");
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryFilterSpinner.setAdapter(categoryAdapter);

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
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String categoryTitle = document.getString("title");
                        if (categoryTitle != null) {
                            categories.add(categoryTitle);
                        }
                    }
                    ((ArrayAdapter) categoryFilterSpinner.getAdapter()).notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
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
        String category = categoryFilterSpinner.getSelectedItem() != null ? categoryFilterSpinner.getSelectedItem().toString() : "Все";
        productAdapter.filter(query, priceSort, category);
    }
}