package com.example.sorapc;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private List<Review> reviews;

    public ReviewAdapter(List<Review> reviews) {
        this.reviews = reviews;
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
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView userName, reviewText, dateText;
        RatingBar ratingBar;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            reviewText = itemView.findViewById(R.id.review_text);
            dateText = itemView.findViewById(R.id.review_date);
            ratingBar = itemView.findViewById(R.id.review_rating);
        }
    }
}