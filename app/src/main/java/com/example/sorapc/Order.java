package com.example.sorapc;

import java.util.Date;
import java.util.List;

/**
 * Класс Order представляет заказ, оформленный пользователем.
 *
 * Основное назначение:
 * - Хранение информации о заказе, включая список товаров, их общую стоимость, комиссию, итоговую сумму, дату заказа и список артикулов.
 *
 * Основные поля:
 * - items: Список товаров (объекты Product), включенных в заказ.
 * - totalPrice: Общая стоимость всех товаров в заказе.
 * - commission: Комиссия за оплату (например, за оплату картой).
 * - finalTotal: Итоговая сумма заказа, включая комиссию.
 * - date: Дата оформления заказа.
 * - articles: Список артикулов товаров для упрощения поиска и идентификации.
 *
 * Конструкторы:
 * - Order(): Пустой конструктор для использования в библиотеках сериализации и десериализации (например, Firebase Firestore).
 * - Order(List<Product> items, long totalPrice, long commission, long finalTotal, Date date, List<String> articles): Конструктор для инициализации всех полей.
 *
 * Геттеры и сеттеры:
 * - Предоставляют доступ и возможность изменения каждого из полей.
 *
 * Особенности:
 * - Поле articles служит для оптимизации поиска товаров в заказе по их артикулу.
 * - Используется в приложении для хранения и обработки информации о заказах.
 */

public class Order {
    private List<Product> items;
    private long totalPrice;
    private long commission;
    private long finalTotal;
    private Date date;
    private List<String> articles; // Список артикулов для упрощения поиска

    public Order() {}

    public Order(List<Product> items, long totalPrice, long commission, long finalTotal, Date date, List<String> articles) {
        this.items = items;
        this.totalPrice = totalPrice;
        this.commission = commission;
        this.finalTotal = finalTotal;
        this.date = date;
        this.articles = articles;
    }

    public List<Product> getItems() { return items; }
    public void setItems(List<Product> items) { this.items = items; }
    public long getTotalPrice() { return totalPrice; }
    public void setTotalPrice(long totalPrice) { this.totalPrice = totalPrice; }
    public long getCommission() { return commission; }
    public void setCommission(long commission) { this.commission = commission; }
    public long getFinalTotal() { return finalTotal; }
    public void setFinalTotal(long finalTotal) { this.finalTotal = finalTotal; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public List<String> getArticles() { return articles; }
    public void setArticles(List<String> articles) { this.articles = articles; }
}