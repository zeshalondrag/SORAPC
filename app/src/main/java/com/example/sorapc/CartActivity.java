package com.example.sorapc;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс CartActivity представляет собой активность для управления корзиной пользователя.
 *
 * Основное назначение:
 * - Отображение товаров, добавленных в корзину.
 * - Обновление и расчет итоговой стоимости и количества товаров.
 * - Переход к оформлению заказа.
 *
 * Основные функции:
 * - Загрузка товаров корзины из Firebase Firestore с привязкой к текущему пользователю.
 * - Слушание изменений в корзине и синхронизация с интерфейсом в реальном времени.
 * - Обновление состояния интерфейса на основе наличия товаров в корзине (например, показ подсказок при пустой корзине).
 * - Слушание изменений в списке избранного для синхронизации состояния "Избранное" у товаров в корзине.
 *
 * Особенности:
 * - Если пользователь не авторизован, активность перенаправляет его на экран авторизации.
 * - Поддержка адаптера RecyclerView для отображения списка товаров.
 * - Интеграция с элементами Header и BottomNavigation для консистентной навигации.
 */

public class CartActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private List<Product> cartList;
    private TextView itemsCountText, totalPriceText, emptyCartText, emptyCartHintText, checkoutHintText;
    private Button checkoutButton;
    private ImageView checkoutHintIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Пожалуйста, авторизуйтесь", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        cartRecyclerView = findViewById(R.id.cart_recycler_view);
        itemsCountText = findViewById(R.id.items_count_text);
        totalPriceText = findViewById(R.id.total_price_text);
        emptyCartText = findViewById(R.id.empty_cart_text);
        emptyCartHintText = findViewById(R.id.empty_cart_hint_text);
        checkoutHintText = findViewById(R.id.checkout_hint_text);
        checkoutHintIcon = findViewById(R.id.checkout_hint_icon);
        checkoutButton = findViewById(R.id.checkout_button);

        cartList = new ArrayList<>();
        cartAdapter = new CartAdapter(this, cartList);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartRecyclerView.setAdapter(cartAdapter);

        loadCartItems();
        listenForFavoritesChanges();

        checkoutButton.setOnClickListener(v -> {
            if (cartList.isEmpty()) {
                Toast.makeText(this, "Корзина пуста", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, CheckoutActivity.class);
            intent.putExtra("cartItems", new ArrayList<>(cartList)); // Передаём список товаров
            startActivity(intent);
        });

        View headerView = findViewById(R.id.header);
        new Header(headerView, this);
        new BottomNavigation(this, R.id.bottom_cart);
    }

    private void loadCartItems() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("cart")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Ошибка загрузки корзины: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    cartList.clear();
                    for (QueryDocumentSnapshot document : value) {
                        Product product = document.toObject(Product.class);
                        cartList.add(product);
                    }

                    cartAdapter.notifyDataSetChanged();
                    updateSummary();
                });
    }

    private void updateSummary() {
        int itemCount = cartList.size();
        itemsCountText.setText("Товары (" + itemCount + ")");

        long totalPrice = 0;
        for (Product product : cartList) {
            totalPrice += product.getPrice() * product.getQuantity();
        }

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        DecimalFormat decimalFormat = new DecimalFormat("#,### ₽", symbols);
        String formattedPrice = decimalFormat.format(totalPrice);
        totalPriceText.setText(formattedPrice);

        emptyCartHintText.setVisibility(cartList.isEmpty() ? View.VISIBLE : View.GONE);
        emptyCartText.setVisibility(cartList.isEmpty() ? View.VISIBLE : View.GONE);
        checkoutHintText.setVisibility(cartList.isEmpty() ? View.VISIBLE : View.GONE);
        checkoutHintIcon.setVisibility(cartList.isEmpty() ? View.VISIBLE : View.GONE);
        cartRecyclerView.setVisibility(cartList.isEmpty() ? View.GONE : View.VISIBLE);
        checkoutButton.setEnabled(!cartList.isEmpty());

        if (cartList.isEmpty()) {
            checkoutButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.disabled_button_color)));
        } else {
            checkoutButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.Aquamarine)));
        }
    }

    private void listenForFavoritesChanges() {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("favorites")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Ошибка синхронизации избранного: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    List<String> favoriteArticles = new ArrayList<>();
                    for (QueryDocumentSnapshot document : value) {
                        Product product = document.toObject(Product.class);
                        favoriteArticles.add(product.getArticle());
                    }
                    for (Product product : cartList) {
                        product.setFavorite(favoriteArticles.contains(product.getArticle()));
                    }
                    cartAdapter.notifyDataSetChanged();
                });
    }
}