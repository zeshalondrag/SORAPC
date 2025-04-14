package com.example.sorapc;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
 * Класс ReviewManagementActivity предоставляет интерфейс для управления отзывами в режиме администратора.
 * Позволяет загружать отзывы из базы данных, отображать их в списке и удалять с подтверждением.
 * Интеграция с Firebase Firestore обеспечивает работу с отзывами.
 * </summary>
 */

public class ReviewManagementActivity extends AppCompatActivity implements ReviewAdapter.OnReviewActionListener {

    private ImageView backIcon;
    private RecyclerView reviewsRecyclerView;
    private TextView noReviewsText; // Добавляем TextView для сообщения "Нет отзывов"
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_management);

        db = FirebaseFirestore.getInstance();

        backIcon = findViewById(R.id.back_icon);
        reviewsRecyclerView = findViewById(R.id.reviews_recycler_view);
        noReviewsText = findViewById(R.id.no_reviews_text); // Инициализируем TextView

        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(this, reviewList, this, true); // Передаём true для isAdminMode
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewsRecyclerView.setAdapter(reviewAdapter);

        backIcon.setOnClickListener(v -> onBackPressed());

        loadReviews();

        View headerView = findViewById(R.id.header);
        new Header(headerView, this);
    }

    private void loadReviews() {
        reviewList.clear(); // Очищаем список перед загрузкой

        // Получаем все продукты из коллекции "products"
        db.collection("products")
                .get()
                .addOnSuccessListener(productSnapshots -> {
                    if (productSnapshots.isEmpty()) {
                        Toast.makeText(this, "Товары не найдены", Toast.LENGTH_SHORT).show();
                        updateNoReviewsVisibility(); // Обновляем видимость сообщения
                        return;
                    }

                    int[] productsProcessed = {0}; // Счётчик обработанных продуктов
                    int totalProducts = productSnapshots.size();

                    // Для каждого продукта загружаем отзывы из подколлекции "reviews"
                    for (QueryDocumentSnapshot productDoc : productSnapshots) {
                        String productId = productDoc.getId();
                        String productName = productDoc.getString("title"); // Получаем название продукта
                        db.collection("products").document(productId)
                                .collection("reviews")
                                .get()
                                .addOnSuccessListener(reviewSnapshots -> {
                                    for (QueryDocumentSnapshot reviewDoc : reviewSnapshots) {
                                        Review review = reviewDoc.toObject(Review.class);
                                        review.setReviewId(reviewDoc.getId()); // Устанавливаем ID отзыва
                                        review.setProductId(productId); // Сохраняем ID продукта
                                        review.setProductName(productName); // Устанавливаем название продукта
                                        reviewList.add(review);
                                    }
                                    productsProcessed[0]++;
                                    // Когда все продукты обработаны, обновляем UI
                                    if (productsProcessed[0] == totalProducts) {
                                        reviewAdapter.notifyDataSetChanged();
                                        updateNoReviewsVisibility();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Ошибка загрузки отзывов для продукта " + productId + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    productsProcessed[0]++;
                                    if (productsProcessed[0] == totalProducts) {
                                        reviewAdapter.notifyDataSetChanged();
                                        updateNoReviewsVisibility();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Ошибка загрузки товаров: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    updateNoReviewsVisibility();
                });
    }

    // Метод для обновления видимости сообщения "Нет отзывов"
    private void updateNoReviewsVisibility() {
        if (reviewList.isEmpty()) {
            noReviewsText.setVisibility(View.VISIBLE);
            reviewsRecyclerView.setVisibility(View.GONE);
        } else {
            noReviewsText.setVisibility(View.GONE);
            reviewsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onEditReview(Review review, int position) {
        // Метод не используется в админ-панели, но должен быть реализован из-за интерфейса
    }

    @Override
    public void onDeleteReview(Review review, int position) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_confirm_delete_category, null);

        TextView messageTextView = dialogView.findViewById(R.id.confirm_delete_message);
        Button confirmButton = dialogView.findViewById(R.id.button_delete_confirm);
        Button cancelButton = dialogView.findViewById(R.id.button_delete_cancel);

        messageTextView.setText("Вы уверены, что хотите удалить этот отзыв?");

        View customTitleView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_title, null);
        TextView titleTextView = customTitleView.findViewById(R.id.dialog_title);
        titleTextView.setText("Подтверждение удаления");

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        builder.setCustomTitle(customTitleView)
                .setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        confirmButton.setOnClickListener(v -> {
            // Удаляем отзыв из подколлекции "reviews" соответствующего продукта
            db.collection("products").document(review.getProductId())
                    .collection("reviews").document(review.getReviewId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Отзыв удалён", Toast.LENGTH_SHORT).show();
                        reviewList.remove(position);
                        reviewAdapter.notifyItemRemoved(position);
                        updateNoReviewsVisibility(); // Обновляем видимость после удаления
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Ошибка удаления отзыва: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
    }
}