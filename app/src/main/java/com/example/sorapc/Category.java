package com.example.sorapc;

/**
 * Класс Category представляет собой модель данных для хранения информации о категории товаров.
 *
 * Основное назначение:
 * - Описывает категорию с уникальным идентификатором (id) и названием категории (title).
 * - Используется для взаимодействия с базой данных и отображения данных о категориях в приложении.
 *
 * Поля:
 * - id: Уникальный идентификатор категории (например, ID в базе данных).
 * - title: Название категории (например, "Электроника", "Одежда").
 *
 * Конструкторы:
 * - Пустой конструктор для инициализации объекта без параметров.
 * - Конструктор с параметром для создания категории с заданным названием.
 *
 * Методы:
 * - Геттеры и сеттеры для доступа и модификации полей id и title.
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