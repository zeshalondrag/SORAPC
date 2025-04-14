package com.example.sorapc;

/**
 * <summary>
 * Класс User представляет модель пользователя в приложении SORAPC.
 * Содержит информацию о пользователе, включая идентификатор, имя, контактные данные и роль.
 * Используется для профилей, аутентификации и управления доступом.
 * </summary>
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