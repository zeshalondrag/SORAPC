package com.example.sorapc;

/**
 * <summary>
 * Класс Category представляет модель данных для описания категории товаров.
 * Он содержит уникальный идентификатор и название категории, а также предоставляет
 * методы для работы с этими данными.
 * </summary>
 */

public class Category {
    private String id;
    private String title;

    public Category() {}

    public Category(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}