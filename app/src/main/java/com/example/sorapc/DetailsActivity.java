package com.example.sorapc;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс DetailsActivity представляет собой активность для отображения подробной информации о товаре.
 *
 * Основное назначение:
 * - Отображение информации о товаре, включая название, цену, описание, характеристики, количество и отзывы.
 * - Предоставление функций для добавления товара в корзину, покупки, оценки и написания отзывов.
 *
 * Основные функции:
 * - Загрузка данных о товаре и его отзывов из базы данных Firebase Firestore.
 * - Добавление товара в корзину с возможностью изменения количества.
 * - Покупка товара с переходом на экран оформления заказа.
 * - Управление списком избранных товаров (добавление/удаление).
 * - Написание отзывов с проверкой права пользователя на добавление отзыва.
 * - Редактирование и удаление отзывов через диалоговые окна.
 * - Динамическое обновление данных о товаре и синхронизация с Firestore.
 *
 * Поля:
 * - product: Объект Product, представляющий текущий товар.
 * - reviews: Список объектов Review, представляющих отзывы о товаре.
 * - cartItems: Карта товаров, добавленных в корзину.
 * - productId: ID текущего товара в Firestore.
 * - auth: Экземпляр FirebaseAuth для авторизации пользователя.
 * - db: Экземпляр FirebaseFirestore для работы с базой данных.
 *
 * Особенности:
 * - Интеграция с Firebase Firestore для работы с товарами, отзывами и корзиной.
 * - Использует библиотеку Glide для загрузки изображений товара.
 * - Обрабатывает случаи отсутствия товара, отзывов и прав пользователя на добавление отзывов.
 * - Предоставляет адаптируемый интерфейс с элементами управления, такими как кнопки и списки.
 */

public class DetailsActivity extends AppCompatActivity implements ReviewAdapter.OnReviewActionListener {
    private TextView productTitle, productPrice, productArticle, productAvailability, productDescription;
    private TextView gpuText, cpuText, motherboardText, coolingText, ramText, ssdText, powerText, caseText;
    private TextView characteristicsHeader, quantityText;
    private LinearLayout characteristicsContainer, quantityLayout;
    private ImageView productImage, favoriteIcon, backIcon, decreaseQuantityButton, increaseQuantityButton;
    private Button addToCartButton, buyNowButton, submitReviewButton;
    private RatingBar ratingBar;
    private EditText reviewInput;
    private RecyclerView reviewsList;
    private Product product;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private boolean isCharacteristicsVisible = false;
    private List<Review> reviews = new ArrayList<>();
    private Map<String, Product> cartItems;
    private String productId; // Для хранения ID продукта в Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Пожалуйста, авторизуйтесь", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        productTitle = findViewById(R.id.product_title);
        productPrice = findViewById(R.id.product_price);
        productArticle = findViewById(R.id.product_article);
        productAvailability = findViewById(R.id.product_availability);
        productDescription = findViewById(R.id.product_description);
        gpuText = findViewById(R.id.gpu_text);
        cpuText = findViewById(R.id.cpu_text);
        motherboardText = findViewById(R.id.motherboard_text);
        coolingText = findViewById(R.id.cooling_text);
        ramText = findViewById(R.id.ram_text);
        ssdText = findViewById(R.id.ssd_text);
        powerText = findViewById(R.id.power_text);
        caseText = findViewById(R.id.case_text);
        characteristicsHeader = findViewById(R.id.characteristics_header);
        characteristicsContainer = findViewById(R.id.characteristics_container);
        productImage = findViewById(R.id.product_image);
        favoriteIcon = findViewById(R.id.favorite_icon);
        backIcon = findViewById(R.id.back_icon);
        addToCartButton = findViewById(R.id.add_to_cart_button);
        buyNowButton = findViewById(R.id.buy_now_button);
        ratingBar = findViewById(R.id.rating_bar);
        reviewInput = findViewById(R.id.review_input);
        submitReviewButton = findViewById(R.id.submit_review_button);
        reviewsList = findViewById(R.id.reviews_list);
        quantityLayout = findViewById(R.id.quantity_layout);
        decreaseQuantityButton = findViewById(R.id.decrease_quantity_button);
        increaseQuantityButton = findViewById(R.id.increase_quantity_button);
        quantityText = findViewById(R.id.quantity_text);

        reviewsList.setLayoutManager(new LinearLayoutManager(this));
        ReviewAdapter reviewAdapter = new ReviewAdapter(this, reviews, this); // Передаём this как Context и OnReviewActionListener
        reviewsList.setAdapter(reviewAdapter);

        product = (Product) getIntent().getSerializableExtra("product");
        if (product == null || product.getArticle() == null) {
            Toast.makeText(this, "Ошибка: товар не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cartItems = new HashMap<>();
        loadCartItems();

        loadProductDetails();
        syncWithFirestore();
        loadReviews();
        checkReviewEligibility();

        backIcon.setOnClickListener(v -> onBackPressed());
        favoriteIcon.setOnClickListener(v -> toggleFavorite());
        characteristicsHeader.setOnClickListener(v -> toggleCharacteristics());
        addToCartButton.setOnClickListener(v -> addToCart());
        buyNowButton.setOnClickListener(v -> buyNow());
        submitReviewButton.setOnClickListener(v -> submitReview());

        decreaseQuantityButton.setOnClickListener(v -> decreaseQuantity());
        increaseQuantityButton.setOnClickListener(v -> increaseQuantity());

        View headerView = findViewById(R.id.header);
        if (headerView != null) {
            new Header(headerView, this);
        }
    }

    private void loadReviews() {
        db.collection("products")
                .whereEqualTo("article", product.getArticle())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot productDoc = queryDocumentSnapshots.getDocuments().get(0);
                        productId = productDoc.getId();

                        db.collection("products").document(productId)
                                .collection("reviews")
                                .addSnapshotListener((value, error) -> {
                                    if (error != null) {
                                        Toast.makeText(this, "Ошибка загрузки отзывов: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    reviews.clear();
                                    if (value != null) {
                                        for (DocumentSnapshot doc : value) {
                                            Review review = doc.toObject(Review.class);
                                            review.setReviewId(doc.getId()); // Сохраняем ID документа отзыва
                                            review.setProductId(productId); // Сохраняем ID продукта
                                            reviews.add(review);
                                        }
                                    }
                                    reviewsList.getAdapter().notifyDataSetChanged();
                                });
                    } else {
                        Toast.makeText(this, "Товар не найден в базе данных", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка поиска товара: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void submitReview() {
        String reviewText = reviewInput.getText().toString().trim();
        float rating = ratingBar.getRating();
        if (reviewText.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, напишите отзыв", Toast.LENGTH_SHORT).show();
            return;
        }
        if (rating == 0) {
            Toast.makeText(this, "Пожалуйста, выберите рейтинг", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String userName = documentSnapshot.getString("name");
                    if (userName == null || userName.isEmpty()) {
                        userName = "Аноним";
                    }

                    Review review = new Review(userId, userName, reviewText, rating, new Date());
                    db.collection("products")
                            .whereEqualTo("article", product.getArticle())
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    DocumentSnapshot productDoc = queryDocumentSnapshots.getDocuments().get(0);
                                    String productId = productDoc.getId();

                                    db.collection("products").document(productId)
                                            .collection("reviews")
                                            .add(review)
                                            .addOnSuccessListener(doc -> {
                                                Toast.makeText(this, "Отзыв добавлен", Toast.LENGTH_SHORT).show();
                                                reviewInput.setText("");
                                                ratingBar.setRating(0);
                                                ratingBar.setVisibility(View.GONE);
                                                reviewInput.setVisibility(View.GONE);
                                                submitReviewButton.setVisibility(View.GONE);
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                } else {
                                    Toast.makeText(this, "Товар не найден в базе данных", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Ошибка поиска товара: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка получения имени пользователя: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onEditReview(Review review, int position) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_edit_review);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        RatingBar editRatingBar = dialog.findViewById(R.id.edit_rating_bar);
        EditText editReviewInput = dialog.findViewById(R.id.edit_review_input);
        Button cancelButton = dialog.findViewById(R.id.cancel_button);
        Button saveButton = dialog.findViewById(R.id.save_button);

        // Предзаполняем данные
        editRatingBar.setRating(review.getRating());
        editReviewInput.setText(review.getText());

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            String newText = editReviewInput.getText().toString().trim();
            float newRating = editRatingBar.getRating();

            if (newText.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, напишите отзыв", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newRating == 0) {
                Toast.makeText(this, "Пожалуйста, выберите рейтинг", Toast.LENGTH_SHORT).show();
                return;
            }

            // Обновляем отзыв в Firestore
            review.setText(newText);
            review.setRating(newRating);
            review.setDate(new Date()); // Обновляем дату

            db.collection("products").document(productId)
                    .collection("reviews")
                    .document(review.getReviewId())
                    .set(review)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Отзыв обновлён", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка обновления отзыва: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        dialog.show();
    }

    @Override
    public void onDeleteReview(Review review, int position) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_delete_review);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button cancelButton = dialog.findViewById(R.id.cancel_button);
        Button deleteButton = dialog.findViewById(R.id.delete_button);

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        deleteButton.setOnClickListener(v -> {
            // Удаляем отзыв из Firestore
            db.collection("products").document(productId)
                    .collection("reviews")
                    .document(review.getReviewId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Отзыв удалён", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        // После удаления отзыва проверяем, может ли пользователь оставить новый отзыв
                        checkReviewEligibility();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка удаления отзыва: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        dialog.show();
    }

    private void checkReviewEligibility() {
        String userId = auth.getCurrentUser().getUid();

        // Проверяем, покупал ли пользователь товар
        db.collection("users").document(userId)
                .collection("orders")
                .whereArrayContains("articles", product.getArticle())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean hasPurchased = !queryDocumentSnapshots.isEmpty();

                    if (!hasPurchased) {
                        ratingBar.setVisibility(View.GONE);
                        reviewInput.setVisibility(View.GONE);
                        submitReviewButton.setVisibility(View.GONE);
                        return;
                    }

                    // Проверяем, оставлял ли пользователь уже отзыв
                    db.collection("products")
                            .whereEqualTo("article", product.getArticle())
                            .get()
                            .addOnSuccessListener(productSnapshots -> {
                                if (!productSnapshots.isEmpty()) {
                                    DocumentSnapshot productDoc = productSnapshots.getDocuments().get(0);
                                    String productId = productDoc.getId();

                                    db.collection("products").document(productId)
                                            .collection("reviews")
                                            .whereEqualTo("userId", userId)
                                            .get()
                                            .addOnSuccessListener(reviewSnapshots -> {
                                                if (!reviewSnapshots.isEmpty()) {
                                                    ratingBar.setVisibility(View.GONE);
                                                    reviewInput.setVisibility(View.GONE);
                                                    submitReviewButton.setVisibility(View.GONE);
                                                    Toast.makeText(this, "Вы уже оставили отзыв на этот товар", Toast.LENGTH_LONG).show();
                                                } else {
                                                    ratingBar.setVisibility(View.VISIBLE);
                                                    reviewInput.setVisibility(View.VISIBLE);
                                                    submitReviewButton.setVisibility(View.VISIBLE);
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(this, "Ошибка проверки отзывов: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Ошибка поиска товара: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка проверки заказов: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadCartItems() {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("cart")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Ошибка загрузки корзины: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    cartItems.clear();
                    for (QueryDocumentSnapshot document : value) {
                        Product cartProduct = document.toObject(Product.class);
                        cartItems.put(cartProduct.getArticle(), cartProduct);
                    }
                    updateAddToCartButton();
                });
    }

    private void loadProductDetails() {
        productTitle.setText(product.getTitle() != null ? product.getTitle() : "Название отсутствует");

        if (product.getImg() != null && !product.getImg().isEmpty()) {
            Glide.with(this)
                    .load(product.getImg())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(productImage);
        } else {
            productImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        DecimalFormat decimalFormat = new DecimalFormat("#,### ₽", symbols);
        productPrice.setText("Цена: " + decimalFormat.format(product.getPrice()));

        productArticle.setText("Артикул: " + (product.getArticle() != null ? product.getArticle() : "Не указано"));

        updateAvailability();

        productDescription.setText(product.getDescription() != null ? product.getDescription() : "Описание отсутствует");

        gpuText.setText(product.getGpu() != null ? product.getGpu() : "Не указано");
        cpuText.setText(product.getCpu() != null ? product.getCpu() : "Не указано");
        motherboardText.setText(product.getMotherboard() != null ? product.getMotherboard() : "Не указано");
        coolingText.setText(product.getCooling() != null ? product.getCooling() : "Не указано");
        ramText.setText(product.getRam() != null ? product.getRam() : "Не указано");
        ssdText.setText(product.getSsd() != null ? product.getSsd() : "Не указано");
        powerText.setText(product.getPower() != null ? product.getPower() : "Не указано");
        caseText.setText(product.getCaseName() != null ? product.getCaseName() : "Не указано");

        updateAddToCartButton();

        updateFavoriteIcon();
    }

    private void syncWithFirestore() {
        db.collection("products")
                .whereEqualTo("article", product.getArticle())
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Ошибка синхронизации: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        DocumentSnapshot document = value.getDocuments().get(0);
                        Product updatedProduct = document.toObject(Product.class);
                        if (updatedProduct != null) {
                            product.setQuantity(updatedProduct.getQuantity());
                            product.setPrice(updatedProduct.getPrice());
                            product.setDescription(updatedProduct.getDescription());
                            product.setGpu(updatedProduct.getGpu());
                            product.setCpu(updatedProduct.getCpu());
                            product.setMotherboard(updatedProduct.getMotherboard());
                            product.setCooling(updatedProduct.getCooling());
                            product.setRam(updatedProduct.getRam());
                            product.setSsd(updatedProduct.getSsd());
                            product.setPower(updatedProduct.getPower());
                            product.setCaseName(updatedProduct.getCaseName());
                            loadProductDetails();
                        }
                    }
                });

        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("favorites")
                .document(product.getArticle())
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Ошибка синхронизации избранного: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    product.setFavorite(value != null && value.exists());
                    updateFavoriteIcon();
                });
    }

    private void updateAvailability() {
        if (product.getQuantity() > 0) {
            productAvailability.setText("Есть в наличии (" + product.getQuantity() + " шт)");
            productAvailability.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        } else {
            productAvailability.setText("Нет в наличии");
            productAvailability.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
        }
    }

    private void updateAddToCartButton() {
        Product cartProduct = cartItems.get(product.getArticle());
        if (cartProduct != null) {
            addToCartButton.setVisibility(View.GONE);
            quantityLayout.setVisibility(View.VISIBLE);
            quantityText.setText(String.valueOf(cartProduct.getQuantity()));
        } else {
            addToCartButton.setVisibility(View.VISIBLE);
            quantityLayout.setVisibility(View.GONE);
        }

        if (product.getQuantity() <= 0) {
            addToCartButton.setText("Нет товара");
            addToCartButton.setEnabled(false);
            addToCartButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.disabled_button_color)));
            buyNowButton.setEnabled(false);
            buyNowButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.disabled_button_color)));
            quantityLayout.setVisibility(View.GONE);
        } else {
            addToCartButton.setText("В корзину");
            addToCartButton.setEnabled(true);
            addToCartButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.Aquamarine)));
            buyNowButton.setEnabled(true);
            buyNowButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.Aquamarine)));
        }
    }

    private void updateFavoriteIcon() {
        favoriteIcon.setImageResource(product.isFavorite() ? R.drawable.heart_pressed : R.drawable.heart_unpressed);
    }

    private void toggleFavorite() {
        String userId = auth.getCurrentUser().getUid();
        if (product.isFavorite()) {
            db.collection("users").document(userId)
                    .collection("favorites").document(product.getArticle())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            db.collection("users").document(userId)
                    .collection("favorites").document(product.getArticle())
                    .set(product)
                    .addOnSuccessListener(aVoid -> {
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void toggleCharacteristics() {
        isCharacteristicsVisible = !isCharacteristicsVisible;
        characteristicsContainer.setVisibility(isCharacteristicsVisible ? View.VISIBLE : View.GONE);
        characteristicsHeader.setText(isCharacteristicsVisible ? "Характеристики ▲" : "Характеристики ▼");
    }

    private void addToCart() {
        if (product.getQuantity() <= 0) {
            Toast.makeText(this, "Товара нет в наличии", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        product.setQuantity(1);
        db.collection("users").document(userId)
                .collection("cart").document(product.getArticle())
                .set(product)
                .addOnSuccessListener(aVoid -> {
                    cartItems.put(product.getArticle(), product);
                    addToCartButton.setVisibility(View.GONE);
                    quantityLayout.setVisibility(View.VISIBLE);
                    quantityText.setText(String.valueOf(product.getQuantity()));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void decreaseQuantity() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Пожалуйста, авторизуйтесь", Toast.LENGTH_SHORT).show();
            return;
        }

        Product cartItem = cartItems.get(product.getArticle());
        if (cartItem != null) {
            int quantity = cartItem.getQuantity();
            if (quantity > 1) {
                quantity--;
                cartItem.setQuantity(quantity);
                String userId = auth.getCurrentUser().getUid();
                int finalQuantity = quantity;
                db.collection("users").document(userId)
                        .collection("cart").document(product.getArticle())
                        .set(cartItem)
                        .addOnSuccessListener(aVoid -> {
                            quantityText.setText(String.valueOf(finalQuantity));
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                String userId = auth.getCurrentUser().getUid();
                db.collection("users").document(userId)
                        .collection("cart").document(product.getArticle())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            cartItems.remove(product.getArticle());
                            addToCartButton.setVisibility(View.VISIBLE);
                            quantityLayout.setVisibility(View.GONE);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        }
    }

    private void increaseQuantity() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Пожалуйста, авторизуйтесь", Toast.LENGTH_SHORT).show();
            return;
        }

        Product cartItem = cartItems.get(product.getArticle());
        if (cartItem != null) {
            String userId = auth.getCurrentUser().getUid();
            db.collection("products")
                    .whereEqualTo("article", product.getArticle())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            Long availableQuantity = queryDocumentSnapshots.getDocuments().get(0).getLong("quantity");
                            if (availableQuantity != null) {
                                int currentQuantity = cartItem.getQuantity();
                                int newQuantity = currentQuantity + 1;

                                if (newQuantity <= availableQuantity) {
                                    cartItem.setQuantity(newQuantity);
                                    db.collection("users").document(userId)
                                            .collection("cart").document(product.getArticle())
                                            .set(cartItem)
                                            .addOnSuccessListener(aVoid -> {
                                                quantityText.setText(String.valueOf(newQuantity));
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                } else {
                                    Toast.makeText(this, "Недостаточно товара на складе", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, "Ошибка: не удалось получить доступное количество", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Товар не найден в базе данных", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка проверки количества: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void buyNow() {
        if (product.getQuantity() <= 0) {
            Toast.makeText(this, "Товара нет в наличии", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        product.setQuantity(1);

        db.collection("users").document(userId)
                .collection("cart").document(product.getArticle())
                .set(product)
                .addOnSuccessListener(aVoid -> {
                    Intent intent = new Intent(this, CheckoutActivity.class);
                    List<Product> cartItems = new ArrayList<>();
                    cartItems.add(product);
                    intent.putExtra("cartItems", new ArrayList<>(cartItems));
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}