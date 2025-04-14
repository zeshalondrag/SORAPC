package com.example.sorapc;

import java.util.Date;

/**
 * Класс Review представляет собой модель для хранения информации об отзыве на продукт.
 *
 * Основное назначение:
 * - Представление данных об отзыве, связанных с конкретным продуктом и пользователем.
 * - Используется для работы с отзывами, таких как добавление, отображение или анализ.
 *
 * Основные поля:
 * - reviewId: Уникальный идентификатор отзыва (ID документа в базе данных).
 * - productId: Уникальный идентификатор продукта, к которому относится отзыв.
 * - productName: Название продукта, к которому относится отзыв.
 * - userId: Уникальный идентификатор пользователя, оставившего отзыв.
 * - userName: Имя пользователя, оставившего отзыв.
 * - text: Текст отзыва.
 * - rating: Рейтинг, выставленный пользователем (например, от 1 до 5).
 * - date: Дата и время, когда был оставлен отзыв.
 *
 * Конструкторы:
 * - Review(): Пустой конструктор для сериализации/десериализации.
 * - Review(String userId, String userName, String text, float rating, Date date): Конструктор для инициализации основных данных отзыва.
 *
 * Геттеры и сеттеры:
 * - Предоставляют доступ к каждому полю и позволяют изменять их значения.
 *
 * Особенности:
 * - Поле reviewId предполагается использовать для хранения уникального ID отзыва, генерируемого базой данных (например, Firestore).
 * - Поля userId и productId используются для связи отзыва с конкретным пользователем и продуктом.
 * - Поле rating представляет собой числовую оценку, которая может быть использована для вычисления средних рейтингов продуктов.
 * - Поле date позволяет сортировать отзывы по дате создания.
 */

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