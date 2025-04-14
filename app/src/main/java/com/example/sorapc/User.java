package com.example.sorapc;

/**
 * Класс User представляет собой модель для хранения информации о пользователе в приложении SORAPC.
 *
 * Основное назначение:
 * - Хранение данных, связанных с пользователем, таких как фамилия, имя, email, телефон и роль.
 * - Используется для работы с профилем пользователя, аутентификацией и авторизацией.
 *
 * Основные поля:
 * - id: Уникальный идентификатор пользователя.
 * - surname: Фамилия пользователя.
 * - name: Имя пользователя.
 * - middlename: Отчество пользователя.
 * - email: Электронная почта пользователя.
 * - phone: Телефонный номер пользователя.
 * - role: Роль пользователя (например, "Administrator" или "Client").
 *
 * Конструкторы:
 * - User(): Пустой конструктор для сериализации/десериализации.
 *
 * Геттеры и сеттеры:
 * - Предоставляют доступ к каждому полю и позволяют изменять их значения.
 *
 * Особенности:
 * - Поле role может быть использовано для управления правами доступа пользователя.
 * - Подходит для использования с Firebase Firestore или другими базами данных для хранения информации о пользователях.
 */

public class User {
    private String id;
    private String surname;
    private String name;
    private String middlename;
    private String email;
    private String phone;
    private String role;

    public User() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMiddlename() {
        return middlename;
    }

    public void setMiddlename(String middlename) {
        this.middlename = middlename;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}