package com.example.sorapc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private List<Review> reviews;
    private Context context;
    private OnReviewActionListener actionListener;
    private FirebaseAuth auth;
    private boolean isAdminMode;

    public interface OnReviewActionListener {
        void onEditReview(Review review, int position);
        void onDeleteReview(Review review, int position);
    }

    public ReviewAdapter(Context context, List<Review> reviews, OnReviewActionListener actionListener) {
        this(context, reviews, actionListener, false);
    }

    public ReviewAdapter(Context context, List<Review> reviews, OnReviewActionListener actionListener, boolean isAdminMode) {
        this.context = context;
        this.reviews = reviews;
        this.actionListener = actionListener;
        this.auth = FirebaseAuth.getInstance();
        this.isAdminMode = isAdminMode;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);

        // Показываем название продукта только в режиме администратора
        if (isAdminMode) {
            holder.productName.setVisibility(View.VISIBLE);
            holder.productName.setText(review.getProductName() != null ? review.getProductName() : "Неизвестный продукт");
        } else {
            holder.productName.setVisibility(View.GONE);
        }

        holder.userName.setText(review.getUserName() != null ? review.getUserName() : "Аноним");
        holder.reviewText.setText(review.getText());
        holder.ratingBar.setRating(review.getRating());
        holder.dateText.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(review.getDate()));

        if (isAdminMode) {
            holder.reviewActions.setVisibility(View.VISIBLE);
            holder.editReviewIcon.setVisibility(View.GONE);
            holder.deleteReviewIcon.setVisibility(View.VISIBLE);
            holder.deleteReviewIcon.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onDeleteReview(review, position);
                }
            });
        } else {
            String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
            if (currentUserId != null && currentUserId.equals(review.getUserId())) {
                holder.reviewActions.setVisibility(View.VISIBLE);
                holder.editReviewIcon.setVisibility(View.VISIBLE);
                holder.deleteReviewIcon.setVisibility(View.VISIBLE);
                holder.editReviewIcon.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onEditReview(review, position);
                    }
                });
                holder.deleteReviewIcon.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onDeleteReview(review, position);
                    }
                });
            } else {
                holder.reviewActions.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView productName, userName, reviewText, dateText;
        RatingBar ratingBar;
        LinearLayout reviewActions;
        ImageView editReviewIcon, deleteReviewIcon;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
            userName = itemView.findViewById(R.id.user_name);
            reviewText = itemView.findViewById(R.id.review_text);
            dateText = itemView.findViewById(R.id.review_date);
            ratingBar = itemView.findViewById(R.id.review_rating);
            reviewActions = itemView.findViewById(R.id.review_actions);
            editReviewIcon = itemView.findViewById(R.id.edit_review_icon);
            deleteReviewIcon = itemView.findViewById(R.id.delete_review_icon);
        }
    }
}