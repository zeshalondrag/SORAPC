package com.example.sorapc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class AdminPanelActivity extends AppCompatActivity {

    private ImageView backIcon;
    private Button manageProductsButton, manageClientsButton, manageCategoriesButton, manageReviewsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        backIcon = findViewById(R.id.back_icon);
        manageProductsButton = findViewById(R.id.manage_products_button);
        manageClientsButton = findViewById(R.id.manage_clients_button);
        manageCategoriesButton = findViewById(R.id.manage_categories_button);
        manageReviewsButton = findViewById(R.id.manage_reviews_button);

        backIcon.setOnClickListener(v -> onBackPressed());

        manageProductsButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminPanelActivity.this, ProductManagementActivity.class);
            startActivity(intent);
        });

        manageClientsButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminPanelActivity.this, ClientManagementActivity.class);
            startActivity(intent);
        });

        manageCategoriesButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminPanelActivity.this, CategoryManagementActivity.class);
            startActivity(intent);
        });

        manageReviewsButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminPanelActivity.this, ReviewManagementActivity.class);
            startActivity(intent);
        });

        View headerView = findViewById(R.id.header);
        new Header(headerView, this);
    }
}