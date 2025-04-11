package com.example.sorapc;

import java.util.Date;

public class Review {
    private String reviewId; // ID документа отзыва
    private String productId; // ID продукта, к которому относится отзыв
    private String productName; // Название продукта
    private String userId;
    private String userName;
    private String text;
    private float rating;
    private Date date;

    public Review() {}

    public Review(String userId, String userName, String text, float rating, Date date) {
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.rating = rating;
        this.date = date;
    }

    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
}