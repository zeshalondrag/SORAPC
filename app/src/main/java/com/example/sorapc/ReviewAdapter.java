package com.example.sorapc;

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

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private List<Review> reviews;
    private OnReviewActionListener actionListener;
    private FirebaseAuth auth;

    public interface OnReviewActionListener {
        void onEditReview(Review review, int position);
        void onDeleteReview(Review review, int position);
    }

    public ReviewAdapter(List<Review> reviews, OnReviewActionListener actionListener) {
        this.reviews = reviews;
        this.actionListener = actionListener;
        this.auth = FirebaseAuth.getInstance();
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
        holder.userName.setText(review.getUserName() != null ? review.getUserName() : "Аноним");
        holder.reviewText.setText(review.getText());
        holder.ratingBar.setRating(review.getRating());
        holder.dateText.setText(new SimpleDateFormat("dd.MM.yyyy").format(review.getDate()));

        // Показываем иконки действий только для отзывов текущего пользователя
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (currentUserId != null && currentUserId.equals(review.getUserId())) {
            holder.reviewActions.setVisibility(View.VISIBLE);
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

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView userName, reviewText, dateText;
        RatingBar ratingBar;
        LinearLayout reviewActions;
        ImageView editReviewIcon, deleteReviewIcon;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
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