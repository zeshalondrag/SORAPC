package com.example.sorapc;

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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DetailsActivity extends AppCompatActivity {
    private TextView productTitle, productPrice, productArticle, productAvailability, productDescription;
    private TextView gpuText, cpuText, motherboardText, coolingText, ramText, ssdText, powerText, caseText;
    private TextView characteristicsHeader;
    private LinearLayout characteristicsContainer;
    private ImageView productImage, favoriteIcon, backIcon;
    private Button addToCartButton, buyNowButton, submitReviewButton;
    private RatingBar ratingBar;
    private EditText reviewInput;
    private RecyclerView reviewsList;
    private Product product;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private boolean isCharacteristicsVisible = false;
    private List<Review> reviews = new ArrayList<>();

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

        reviewsList.setLayoutManager(new LinearLayoutManager(this));
        ReviewAdapter reviewAdapter = new ReviewAdapter(reviews);
        reviewsList.setAdapter(reviewAdapter);

        product = (Product) getIntent().getSerializableExtra("product");
        if (product == null || product.getArticle() == null) {
            Toast.makeText(this, "Ошибка: товар не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProductDetails();
        syncWithFirestore();
        loadReviews();

        backIcon.setOnClickListener(v -> onBackPressed());
        favoriteIcon.setOnClickListener(v -> toggleFavorite());
        characteristicsHeader.setOnClickListener(v -> toggleCharacteristics());
        addToCartButton.setOnClickListener(v -> addToCart());
        buyNowButton.setOnClickListener(v -> buyNow());
        submitReviewButton.setOnClickListener(v -> submitReview());

        View headerView = findViewById(R.id.header);
        if (headerView != null) {
            new Header(headerView, this);
        }
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

    private void loadReviews() {
        db.collection("products")
                .whereEqualTo("article", product.getArticle())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot productDoc = queryDocumentSnapshots.getDocuments().get(0);
                        String productId = productDoc.getId();

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
        if (product.getQuantity() <= 0) {
            addToCartButton.setText("Нет товара");
            addToCartButton.setEnabled(false);
            addToCartButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.disabled_button_color)));
            buyNowButton.setEnabled(false);
            buyNowButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.disabled_button_color)));
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
            // Добавляем в избранное
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
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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